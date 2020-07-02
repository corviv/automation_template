package com.github.corviv;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Listeners;

public class UDPUtils {

    @Listeners(LoggerListener.class)
    public static class EchoServerUDP extends Thread {

        public enum Mode {
            CHECK_CONDITIONS,
            PROMISCUOUS,
            ECHO
        }

        public enum State {
            IDLE,
            RUNNING,
            CONDITIONS_ARE_MET,
            TIMEOUT
        }

        private DatagramSocket socket = null;
        private byte[] buf = new byte[256];
        private String datagram = null;
        private int bindPort = 0;
        private int rcvPort = 0;
        private String rcvAddress = null;
        private String srcAddress = null;
        private int rcvTimeoutMs = 0;
        private String specMsg = null;
        private int srcPort = 0;

        private Mode currentMode = Mode.CHECK_CONDITIONS;
        private State currentState = State.IDLE;

        private static final Logger logger = LoggerFactory.getLogger("EchoServerUDP");

        public EchoServerUDP(int bindPort) throws SocketException {
            this.bindPort = bindPort;
            try {
                socket = new DatagramSocket(bindPort);
            } catch (BindException e) {
                throw new RuntimeException("Port '" + bindPort + "' already in use!");
            }
        }

        public State getCurrentState() {
            return currentState;
        }

        public boolean isConditionsMet() {
            return currentState == State.CONDITIONS_ARE_MET;
        }

        public void enablePromiscuousMode() {
            currentMode = Mode.PROMISCUOUS;
        }

        public void enableEcho() {
            currentMode = Mode.ECHO;
        }

        public void setSrcAddress(String srcAddress) {
            this.srcAddress = srcAddress;
        }

        public void setSrcPort(int srcPort) {
            this.srcPort = srcPort;
        }

        public void setSpecMsg(String specMsg) {
            this.specMsg = specMsg;
        }

        public void setTimeout(int rcvTimeoutMs) throws SocketException {
            this.rcvTimeoutMs = rcvTimeoutMs;
            socket.setSoTimeout(rcvTimeoutMs);
        }

        public boolean isTimeout() {
            return currentState == State.TIMEOUT;
        }

        private boolean checkConditions(DatagramPacket packet) {
            if (srcAddress != null) {
                if (!srcAddress.equals(rcvAddress)) {
                    logger.info("Received package from {}", rcvAddress);
                    logger.warn("Conditions weren't met!");
                    return false;
                }
            }
            if (srcPort != 0) {
                if (srcPort != rcvPort) {
                    logger.info("Received packet from {}:{}", rcvAddress, rcvPort);
                    logger.warn("Conditions weren't met!");
                    return false;
                }
            }
            if (specMsg != null) {
                if (!datagram.equals(specMsg)) {
                    logger.info("Received packet from {}:{} with message '{}'", rcvAddress, rcvPort,
                        datagram);
                    logger.warn("Conditions weren't met!");
                    return false;
                }
            }
            logger.info("Received packet from {}:{} with message '{}'", rcvAddress, rcvPort,
                datagram);
            logger.info("Conditions have been met successfully!");
            currentState = State.IDLE;
            return true;
        }

        private void sendEcho(DatagramPacket packet) throws IOException {
            InetAddress address = packet.getAddress();
            packet = new DatagramPacket(buf, buf.length, address, packet.getPort());
            socket.send(packet);
            logger.info("Echo packet was sent..");
        }

        public void run() {
            currentState = State.RUNNING;
            StringBuilder status = new StringBuilder("Server was started. Conditions: {");

            if (srcAddress != null) {
                status.append("src_addr: ").append(srcAddress);
            }
            if (srcPort != 0) {
                status.append(", ").append("src_port: ").append(srcPort);
            }
            if (specMsg != null) {
                status.append(", ").append("spec_msg: ").append(specMsg);
            }

            logger.info("{}}. Start listening port {}..", status, bindPort);
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);

                while (true) {
                    socket.receive(packet);
                    rcvAddress = packet.getAddress().getHostAddress();
                    rcvPort = packet.getPort();
                    datagram = new String(packet.getData(), 0, packet.getLength());

                    switch (currentMode) {
                        case CHECK_CONDITIONS:
                            if (checkConditions(packet)) {
                                currentState = State.CONDITIONS_ARE_MET;
                            }
                            break;
                        case PROMISCUOUS:
                            logger.info("Received packet from {}:{} with message '{}'", rcvAddress,
                                rcvPort, datagram);
                            continue;
                        case ECHO:
                            logger.info("Received packet from {}:{} with message '{}'", rcvAddress,
                                rcvPort, datagram);
                            sendEcho(packet);
                            continue;
                    }
                    break;
                }
            } catch (SocketTimeoutException e) {
                currentState = State.TIMEOUT;
                logger.info("Timeout!");
                logger.info("The packet was not received during the specified timeout: {} ms",
                    rcvTimeoutMs);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                socket.close();
            }
        }

        public void close() {
            currentState = State.IDLE;
            socket.close();
        }
    }

    @Listeners(LoggerListener.class)
    public static class ClientUDP {

        private DatagramSocket socket;
        private InetAddress address;
        private int port;
        private int sendPeriodMs = 1000;
        private String specMsg = "";
        private static final Logger logger = LoggerFactory.getLogger("ClientUDP");

        public enum Mode {
            SINGLE,
            CONTINUOUS
        }

        public enum State {
            IDLE,
            RUNNING
        }

        private Mode currentMode = Mode.SINGLE;
        private State currentState = State.IDLE;

        public ClientUDP(String dstAddress, int dstPort)
            throws SocketException, UnknownHostException {
            socket = new DatagramSocket();
            port = dstPort;
            address = InetAddress.getByName(dstAddress);
        }

        public void enableContinuousMode(int sendPeriodMs) {
            currentMode = Mode.CONTINUOUS;
            this.sendPeriodMs = sendPeriodMs;
        }

        public State getCurrentState() {
            return currentState;
        }

        public void setPeriod(int sendPeriodMs) {
            this.sendPeriodMs = sendPeriodMs;
        }

        public void setSpecMsg(String Msg) {
            specMsg = Msg;
        }

        public void send() throws IOException {
            byte[] buf = specMsg.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
            socket.send(packet);
            logger.info("Packet with message '{}' sent to {}:{}", specMsg, address, port);
        }

        public void run() {
            try {
                currentState = State.RUNNING;
                while (true) {

                    switch (currentMode) {
                        case SINGLE:
                            send();
                            break;
                        case CONTINUOUS:
                            send();
                            Thread.sleep(sendPeriodMs);
                            continue;
                    }
                    break;
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                currentState = State.IDLE;
            }
        }

        public void close() {
            currentState = State.IDLE;
            socket.close();
        }
    }
}
