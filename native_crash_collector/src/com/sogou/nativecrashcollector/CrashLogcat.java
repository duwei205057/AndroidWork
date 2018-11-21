package com.sogou.nativecrashcollector;

import java.util.ArrayList;


/**
 * 收集崩溃时的logcat信息
 * @author huangjun
 * @date 18-3-1
 */ // TODO 能否动态配置收集条件
public class CrashLogcat {
    public static final String FORMAT = "threadtime";
    public static final String DEFAULT_OUTPUT_LOG_FILE_PATH = "/data/data/com.dw.debug/files/logcat.txt";
    public static final String LINE_SEPARATOR = "--- --- --- --- --- --- --- --- ---\n";
    // TODO 日志文件需限制大小
    public static ShellUtils.CommandResult collectLogcat(String logFilePath, int count) {
        if (logFilePath == null || logFilePath.length() == 0) {
            logFilePath = DEFAULT_OUTPUT_LOG_FILE_PATH;
        }
        ArrayList<String> commnandList = new ArrayList<>();

        // 不删除之前的logcat文件
//        commnandList.add(String.format("rm -f %s", logFilePath));
//        String cmd = String.format("logcat -d -v %s -f %s -T %d *", FORMAT, logFilePath, count);
        String cmd = String.format("logcat -v %s -t %d * ", FORMAT, count);
//        String cmd = String.format("logcat -v %s -t %d * >> %s", FORMAT, count, logFilePath);
        commnandList.add(String.format("echo '%s' >> %s", LINE_SEPARATOR, logFilePath));
        commnandList.add(String.format("echo 'Crash LOGCAT: %s' >> %s", cmd, logFilePath));
        commnandList.add(cmd);
        ShellUtils.CommandResult result = ShellUtils.execCommand(commnandList, false);
        return result;
    }
}
