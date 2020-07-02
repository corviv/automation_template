package com.github.corviv;

import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellNotAvailableException;
import com.profesorfalken.jpowershell.PowerShellResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Listeners;

@Listeners(LoggerListener.class)
public class CommandUtils {

    private static final Logger logger = LoggerFactory.getLogger("CommandUtils");
    static boolean isSuccessfully = false;

    public static boolean comparisonOutputCmd(String command, String lineBegin, String lineEnd)
        throws IOException {
        Process cmdRun;
        boolean result = false;
        cmdRun = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(cmdRun.getInputStream(), "Cp866"));
        String line;

        while ((line = reader.readLine()) != null) {
            logger.debug("current line: " + line);
            if (line.contains(lineBegin) && line.contains(lineEnd)) {
                result = true;
                break;
            }
        }
        //logger.debug(result);
        return result;
    }

    public static String executePsProcess(String process) {
        return executePsCommand("Start-Process " + process, 2000);
    }

    public static String executePsProcess(String process, int maxWaitMs) {
        return executePsCommand("Start-Process " + process, maxWaitMs);
    }

    public static String executePsCommand(String command) {
        PowerShellResponse psResponse = PowerShell.executeSingleCommand(command);
        if (psResponse.isError()) {
            isSuccessfully = false;
            logger.info("Command '{}' execution error!", command);
        } else {
            logger.info("Command '{}' completed successfully!", command);
            isSuccessfully = true;
        }
        String commandResponse = psResponse.getCommandOutput().trim();
        logger.info("PS: \n{}\n", commandResponse);
        return commandResponse;
    }

    public static String executePsCommand(String command, int maxWaitMs) {
        Map<String, String> config = new HashMap<>();
        config.put("maxWait", Integer.toString(maxWaitMs));
        try (PowerShell powerShell = PowerShell.openSession()) {
            PowerShellResponse psResponse = powerShell
                .executeCommand(command);
            if (psResponse.isError()) {
                isSuccessfully = false;
                logger.info("Command '{}' execution error!", command);
            } else {
                logger.info("Command '{}' completed successfully!", command);
                isSuccessfully = true;
            }
            String commandResponse = psResponse.getCommandOutput().trim();
            logger.info("PS: \n{}\n", commandResponse);
            return commandResponse;
        } catch (PowerShellNotAvailableException e) {
            logger.info("PowerShell is not available in the system!");
            return null;
        }
    }

    /*
    public static String executePsScript(String scriptPath, int maxWaitMs) {
        try (PowerShell powerShell = PowerShell.openSession()) {
            //Map<String, String> config = new HashMap<String, String>();
            //config.put("maxWait", Integer.toString(maxWaitMs));
            PowerShellResponse response = powerShell.executeScript(scriptPath);
            if (response.isError()) {
                isSuccessfully = false;
                logger.info("Script execution error!");
            } else {
                logger.info("Script completed successfully!");
                isSuccessfully = true;
            }
            String scriptResponse = response.getCommandOutput().trim();
            logger.info("Script output: {}", scriptResponse);
            return scriptResponse;
        } catch (PowerShellNotAvailableException ex) {
            logger.info("PowerShell is not available in the system!");
            return null;
        }
    }*/

    public static String executePsScript(String scriptPath) {
        return executeCommand("powershell " + scriptPath);
    }

    public static String executePyScript(String scriptPathName, String... args) {
        String argsLine = String.join(" ", args);
        return executeCommand("python " + scriptPathName + " " + argsLine);
    }

    public static String executeCmdCommand(String command) {
        return executeCommand("cmd /c " + command);
    }

    public static String executeBashCommand(String command) {
        return executeCommand("/bin/bash " + command);
    }

    public static String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            process.getErrorStream().close();
            process.getInputStream().close();
            process.getOutputStream().close();
            int exitVal = process.waitFor();

            if (exitVal == 0) {
                logger.info("Command '{}' completed successfully!", command);
                logger.debug(output.toString());
                isSuccessfully = true;
            } else {
                logger.info("Command '{}' execution error!", command);
                logger.info("Exit code: {}", exitVal);
                isSuccessfully = false;
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            isSuccessfully = false;
        }

        return output.toString();
    }

    public static boolean killProcess(String processName) {
        CommandUtils.executeCommand("taskkill /F /IM " + processName + ".exe");
        if (CommandUtils.isSuccessfully) {
            logger.debug("Process killed successfully!\n");
            return true;
        }
        logger.debug("Process not found..\n");
        return false;
    }

    public static boolean killProcess(Process process) {
        try {
            process.destroy();
            if (!process.waitFor(3, TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }
            if (!process.isAlive()) {
                logger.debug("Process destroyed successfully!\n");
                return true;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.debug("Process not found..\n");
        return false;
    }

    public static class ExecuteCommandInFlow {

        private ExecutorService executor
            = Executors.newSingleThreadExecutor();

        public Future executeCmd(String command) {
            return executor.submit(() -> {
                executeCmdCommand(command);
            });
        }
    }

    public static Future executeThreadedCommand(String command) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        return executor.submit(() -> {
            executeCommand(command);
        });
    }

    public static String executeThreadedProcess(String command, int timeoutMs) {
        Future<?> fut = executeThreadedCommand(command);
        String res = "";
        try {
            res = (String) fut.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return res;
    }
}
