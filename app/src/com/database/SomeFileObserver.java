package com.database;

import android.os.FileObserver;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;


/**
 * Created by dw on 18-5-9.
 */

public class SomeFileObserver extends FileObserver {

    String rootPath;

    public SomeFileObserver(String path) {
        super(path);
        rootPath = path;
    }

    @Override
    public void onEvent(int event, @Nullable String path) {
        int el = event & FileObserver.ALL_EVENTS;
        LOGD("onEvent event:" + el + " path:" + path);
        switch (el){
            case FileObserver.CREATE:
                File f = new File(rootPath + path);
                f.renameTo(new File(rootPath + path + ".bak"));
                break;
            default:
                break;
        }
    }

    private void LOGD(String message){
        Log.d("xx", message);
    }
}
