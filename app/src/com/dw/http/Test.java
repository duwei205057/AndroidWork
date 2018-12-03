package com.dw.http;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by dw on 18-11-29.
 */

public class Test implements Handler.Callback{
    HandlerThread ht;
    Handler handler ;

    public Test() {
        ht = new HandlerThread("Test");
        ht.start();
        handler = new Handler(ht.getLooper(), this);
    }

    public void httpsConnect() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://mybank.icbc.com.cn/icbc/newperbank/perbank3/includes/preventLowerMacSysVersionTip.jsp?flag=c");
                    URLConnection urlConnection = url.openConnection();
                    InputStream in = urlConnection.getInputStream();
                    byte[] buffer = new byte[1024];
                    int c = 0;
                    PrintStream out = System.out;
                    while ((c = in.read(buffer)) != -1) {
                        out.write(buffer, 0, c);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }
}
