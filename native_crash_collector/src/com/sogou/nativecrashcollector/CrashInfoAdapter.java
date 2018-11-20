package com.sogou.nativecrashcollector;

/**
 * Created by dw on 18-11-2.
 */

public interface CrashInfo {

    String getCrashMessage();

    String getCrashMessage(Thread crashThread);
}
