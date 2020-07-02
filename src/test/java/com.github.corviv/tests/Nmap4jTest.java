package com.github.corviv.tests;

import com.github.corviv.INmap4j;
import com.github.corviv.LoggerListener;
import com.github.corviv.UDPUtils.ClientUDP;
import com.github.corviv.UDPUtils.EchoServerUDP;
import com.github.corviv.Utils.MyDataEnumerator;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(LoggerListener.class)
public class Nmap4jTest {

    private static final Logger logger = LoggerFactory.getLogger(Nmap4jTest.class);
    static private final String dstHost[] = {"localhost"};

    @DataProvider(name = "MyDataEnumerator")
    public Iterator<Object[]> MyDataEnumerator() {
        return new MyDataEnumerator(3000, 3010, 134, 136);
    }

    @Test(priority = 1, dataProvider = "MyDataEnumerator")
    public void testScanTCP(int srcPort, int[] dstPorts) {
        INmap4j nmapRunner = new INmap4j();
        nmapRunner.scanTCP(srcPort, dstPorts, dstHost);

        for (int iport = 0; iport < INmap4j.getScannedPortsCount(); iport++) {
            logger.info("PortID: " + INmap4j.getPortNumber(iport));
            String status = INmap4j.getPortState(iport);
            Assert.assertEquals(status, INmap4j.portState.CLOSED);
        }
    }

    @DataProvider(name = "MyDataEnumerator2")
    public Iterator<Object[]> MyDataEnumerator2() {
        return new MyDataEnumerator(3000, 3010);
    }

    @DataProvider(name = "MyDataEnumerator3")
    public Object[][] MyDataEnumerator3() {
        return new Object[][]{{3000}, {3010}};
    }

    @Test(priority = 1, dataProvider = "MyDataEnumerator3")
    public void testScanTCP2(int srcPort, int[] dstPort) {
        INmap4j nmapRunner = new INmap4j();
        nmapRunner.scanTCP(srcPort, dstPort, dstHost);
    }

    @Test(priority = 1)
    public void testScanTCP3() {
        INmap4j nmapRunner = new INmap4j();
        nmapRunner.scanTCP(3000, dstHost);
    }

    @Test(priority = 1)
    public void testSendUDP() {
        INmap4j nmapRunner = new INmap4j();
        nmapRunner.sendUDP(3010, new int[]{3001}, dstHost);
    }

    @Test
    public void checkUDPClient()
        throws SocketException, UnknownHostException, InterruptedException {
        ClientUDP test = new ClientUDP("127.0.0.1", 3001);
        //test.enableContinuousMode(1000);
        test.setSpecMsg("test1");
        test.run();
        test.setSpecMsg("test2");
        test.run();
        test.setSpecMsg("test3");
        test.run();
        test.setSpecMsg("test4");
        test.run();
    }

    @Test
    public void checkUDPServer() throws SocketException {
        EchoServerUDP test = new EchoServerUDP(3001);
        test.setTimeout(10000);
        test.setSrcPort(3000);
        //test.setSpecMsg("test1");
        //test.enablePromiscuousMode();
        //test.enableEcho();
        test.run();
        //Assert.assertFalse(test.isTimeout());
        Assert.assertTrue(test.isConditionsMet());
    }
}

