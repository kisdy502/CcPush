package tv.fengmang.ccpush.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Android Java代码执行adb Shell命令 通过java代码代替adb命令 增加工具类
 */
public class ShellUtil {

    public static final String COMMAND_SU = "su";
    public static final String COMMAND_SH = "sh";
    public static final String COMMAND_EXIT = "exit\n";
    public static final String COMMAND_LINE_END = "\n";
    private static final String TAG = "ShellUtil";

    private ShellUtil() {
        throw new AssertionError();
    }


    public static boolean checkRootPermission() {
        return execCommand("echo root", true, false).result == 0;
    }


    public static CommandResult execCommand(String command, boolean isRoot) {
        return execCommand(new String[]{command}, isRoot, true);
    }


    public static CommandResult execCommand(List commands, boolean isRoot) {
        return execCommand(
                commands == null ? null
                        : (String[]) commands.toArray(new String[]{}), isRoot,
                true);
    }


    public static CommandResult execCommand(String[] commands, boolean isRoot) {
        return execCommand(commands, isRoot, true);
    }


    public static CommandResult execCommand(String command, boolean isRoot,
                                            boolean isNeedResultMsg) {
        return execCommand(new String[]{command}, isRoot, isNeedResultMsg);
    }


    public static CommandResult execCommand(List commands, boolean isRoot,
                                            boolean isNeedResultMsg) {
        return execCommand(
                commands == null ? null
                        : (String[]) commands.toArray(new String[]{}), isRoot,
                isNeedResultMsg);
    }


    public static CommandResult execCommand(String[] commands, boolean isRoot,
                                            boolean isNeedResultMsg) {
        int result = -1;
        if (commands == null || commands.length == 0) {
            return new CommandResult(result, null, null);
        }

        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;

        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec(
                    isRoot ? COMMAND_SU : COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }
                //ELog.i("cmd:" + command);
                // donnot use os.writeBytes(commmand), avoid chinese charset
                // error
                os.write(command.getBytes());
                os.writeBytes(COMMAND_LINE_END);
                os.flush();
            }
            os.writeBytes(COMMAND_EXIT);
            os.flush();

            result = process.waitFor();
            // get command result
            if (isNeedResultMsg) {
                successMsg = new StringBuilder();
                errorMsg = new StringBuilder();
                successResult = new BufferedReader(new InputStreamReader(
                        process.getInputStream()));
                errorResult = new BufferedReader(new InputStreamReader(
                        process.getErrorStream()));
                String s;
                while ((s = successResult.readLine()) != null) {
                    successMsg.append(s);
                }
                while ((s = errorResult.readLine()) != null) {
                    errorMsg.append(s);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IOException:" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception:" + e.getMessage());
        } finally {
            close(successResult);
            close(errorResult);
            close(os);
            if (process != null) {
                process.destroy();
            }
        }
        return new CommandResult(result, successMsg == null ? null
                : successMsg.toString(), errorMsg == null ? null
                : errorMsg.toString());
    }


    public static class CommandResult {

        /**
         * result of command
         **/
        public int result;
        /**
         * success message of command result
         **/
        public String successMsg;
        /**
         * error message of command result
         **/
        public String errorMsg;

        public CommandResult(int result) {
            this.result = result;
        }

        public CommandResult(int result, String successMsg, String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }

        public boolean containsInstallError(String error) {
            return null != errorMsg && null != error
                    && errorMsg.toLowerCase().contains(error.toLowerCase());
        }

        @Override
        public String toString() {
            return "result =" + result + ":: suc =" + successMsg + ":: err ="
                    + errorMsg;
        }
    }

    public static String execRootCMD(String cmd) {
        return execRootCMD(cmd, true);
    }

    public static String execRootCMD(String cmd, boolean isroot) {
        CommandResult res = ShellUtil.execCommand(cmd, isroot);
        return "cmd=" + cmd + ":: result =" + res.result + ":: suc =" + res.successMsg + ":: err ="
                + res.errorMsg;
    }

    public static CommandResult executeCmd(String cmd, boolean isroot) {
        CommandResult res = ShellUtil.execCommand(cmd, isroot);
        return res;
    }

    public static void close(Closeable... closeable) {
        for (Closeable c : closeable) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}