package com.sogou.nativecrashcollector;

import android.os.Looper;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * Created by dw on 18-10-16.
 */

//@Keep
public class CrashCollectUtils {

    private static final boolean DEBUG = true;
    //    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "xx";

    //    @Keep
    public static String getThreadStackTrace(int tid){

//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        StringBuilder stackTrace = new StringBuilder();
        stackTrace.append(getMessageFromCallback());
        stackTrace.append(getMessageFromShell());
        stackTrace.append(getMessageFromThread(tid));

        return stackTrace.toString();
    }

    private static String getMessageFromCallback() {
        CrashInfo crashInfo = NativeCrashManager.getInstance().getmCrashInfo();
        if (crashInfo != null) {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("[App CrashInfo]\n");
                sb.append(crashInfo.getCrashMessage());
                sb.append("\n\n");
                return sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    private static String getMessageFromShell() {
        if (NativeCrashManager.sCollectLogcat) {
            StringBuilder sb = new StringBuilder();
            ShellUtils.CommandResult result = CrashLogcat.collectLogcat(null, 500);
            LOGD("getThreadStackTrace result=="+ result.result+" errorMsg=="+result.errorMsg+" successMsg=="+result.successMsg);
            if (result != null) {
                sb.append("[Backtrace logcat]\n");
                sb.append(result.successMsg);
            }
            return sb.toString();
        }
        return "";
    }

    private static String getMessageFromThread(int tid) {
        if (tid <= 0) return "";
        StringBuilder sb = new StringBuilder();
        String threadName = getThreadNameById(tid);
        Thread theThread = getThreadByName(threadName);
        LOGD("getThreadStackTrace  tid="+tid+" threadName="+threadName+" theThread="+theThread);
        sb.append("[Backtrace java]\n");
        if (theThread == null) return "";
        for (StackTraceElement ste : theThread.getStackTrace()) {
            sb.append("\t at  "+ste.getClassName() + "." + ste.getMethodName() + "(" +ste.getClassName()+ ".java:" + ste.getLineNumber() + ")" + "\n");
        }
        return sb.toString();
    }

    private static Thread getThreadByName(String threadName)
    {
        if ("main".equals(threadName)) {
            return Looper.getMainLooper().getThread();
        } else {
            Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
            for(Thread thread: threadSet) {
                String name = thread.getName();
                if ( threadName.equals(name) ) {
                    return thread;
                }
            }

        }
        return null;
    }

    private static String getThreadNameById(int tid)
    {
        String strName = "";
        int pid = android.os.Process.myPid();
        if ( pid == tid ) {
            return "main";
        }
        String pathNameFile = String.format("/proc/%d/task/%d/comm", pid, tid);
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(pathNameFile);
            if (null != fin) {
                byte[] byteName = new byte[1024];
                int nSize = fin.read(byteName, 0, 1024);
                String strOrg = new String(byteName, 0, nSize, Charset.forName("UTF-8"));
                int nReturn = strOrg.indexOf('\n');
                if (nReturn >= 0) {
                    strName = strOrg.substring(0, nReturn);
                } else {
                    strName = strOrg;
                }
            }
        } catch (IOException ioe) {
            // io exception, do nothing
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strName;
    }


    private static void LOGD(String message) {
        if (DEBUG)
            Log.d(TAG, message);
    }
}
