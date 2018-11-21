package com.sogou.nativecrashcollector;

/**
 * Created by dw on 18-11-2.
 */

public abstract class CrashInfoAdapter implements CrashInfo{

        public String getCrashMessage() {
            return "";
        }

}

interface CrashInfo {

    String getCrashMessage();

}