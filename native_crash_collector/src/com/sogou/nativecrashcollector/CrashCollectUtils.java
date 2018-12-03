package com.sogou.nativecrashcollector;

import android.os.Looper;
import android.text.TextUtils;
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
    private static final String DEFAULT = "";

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
        //associate with tid
        stackTrace.append(getMessageFromThread(tid));

        return stackTrace.toString();
    }

    private static String getMessageFromCallback() {
        CrashInfo crashInfo = NativeCrashManager.getInstance().getmCrashInfo();
        if (crashInfo != null) {
            try {
                String message = crashInfo.getCrashMessage();
                if (TextUtils.isEmpty(message)) return DEFAULT;
                StringBuilder sb = new StringBuilder();
                sb.append("[App CrashInfo]\n");
                sb.append(message);
                sb.append("\n\n");
                LOGD("getMessageFromCallback="+sb.toString());
                return sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return DEFAULT;
    }

    private static String getBackTraceFromAspect(Thread thread) {
        String message = BacKTraceFactory.getService().getBackTrace(thread);
        if (TextUtils.isEmpty(message)) return DEFAULT;
        StringBuilder sb = new StringBuilder();
        sb.append("[Backtrace java - 1]\n");
        sb.append(message);
        sb.append("\n\n");
        LOGD("getMessageFromCallback="+sb.toString());
        return sb.toString();
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
        return DEFAULT;
    }

    private static String getMessageFromThread(int tid) {
        Thread thread = getThreadByTid(tid);
        StringBuilder sb = new StringBuilder();
        if (thread != null) {
            sb.append("Java Thread Name : "+thread.getName()+"\n");
            String message = getBackTraceFromAspect(thread);
            if (TextUtils.isEmpty(message)) {
                sb.append(getBackTraceNow(thread));
            } else {
                sb.append(message);
            }
        }
        return sb.toString();
    }

    private static String getBackTraceNow(Thread thread) {
        StringBuilder sb = new StringBuilder();
        if (thread == null) return DEFAULT;
        sb.append("[Backtrace java - 2]\n");
        for (StackTraceElement ste : thread.getStackTrace()) {
            sb.append("\t at  "+ste.getClassName() + "." + ste.getMethodName() + "(" +ste.getClassName()+ ".java:" + ste.getLineNumber() + ")" + "\n");
        }
        return sb.toString();
    }

    private static Thread getThreadByTid(int tid) {
        if (tid <= 0) return null;
        StringBuilder sb = new StringBuilder();
        String threadName = getThreadNameById(tid);
        Thread theThread = getThreadByName(threadName);
        LOGD("getThreadStackTrace  tid="+tid+" threadName="+threadName+" theThread="+theThread);
        return theThread;
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
        String strName = DEFAULT;
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
