package com.sogou.nativecrashcollector;

/**
 * Created by dw on 18-11-20.
 */

public class BacKTraceFactory {

    public static BackTraceService getService() {
        return BackTraceUtils.getInstance();
    }

}


interface BackTraceService {

    public void register();

    public void withdraw();

    public String getBackTrace(Thread thread);
}