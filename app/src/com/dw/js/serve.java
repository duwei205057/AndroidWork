package com.dw.js;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.dw.R;

/**
 * 2014年6月26日 11:10:47
 *
 * @author yung
 */
@SuppressLint("JavascriptInterface")
public class serve extends Activity {

    WebView myWebView;
    // 存在data/data目录下的html文件名
    String HTMLNAME = "serve.html";
    // 存在data/data目录下的填充html的js的文件名

    String JSNAME = "JSData.data";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.serve);
        myWebView = (WebView) findViewById(R.id.serve_vebview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        isExistsHTML();
        // myWebView.loadUrl("file:///android_asset/ss.html");
        String path = getFilesDir().getAbsolutePath() + HTMLNAME; // data/data目录
        myWebView.loadUrl("file:///" + path);
        myWebView
                .addJavascriptInterface(new AndroidJavaScript(this), "Android");

        // myWebView.loadUrl("javascript:getStr('" + 122222 + "')");
        myWebView.setWebViewClient(webviewcilnt);
        String phon = "技术服务电话：,0731-22332233,0731-44332234;产品服务QQ:,5733935198,209384022;产品公众微信:,CSHNJK,yung7086,weixin";
//		String path1 = "file:///android_asset/wxicon.png";

        saveHTMLData(phon);
    }

    WebViewClient webviewcilnt = new WebViewClient() {
        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            Log.d("xx", "onReceivedError===" + view);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            String phon = loadHTMLData();
            myWebView.loadUrl("javascript:createTable('" + phon + "')");
            // 获取webview加载的html页面
//            view.loadUrl("javascript:window.Android.getSource('<html>'+"
//                    + "document.getElementsByTagName('html')[0].innerHTML+'</html>');");

//			Log.d("xx", "myWebView==="+myWebView);
//			Log.d("xx", "view==="+view);
            Log.d("xx", "onPageFinished===" + view);

        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.d("xx", "onPageStarted===" + view);
        }
    };

    public void upload(View view) {
        updateFile();
    }


    private void updateFile() {
        AsyncTask<Void, Integer, Boolean> ast = new AsyncTask<Void, Integer, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    URL url = new URL("http://localhost:9090/AndroidServer/FirstServlet");
                    HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                    huc.setConnectTimeout(3000);
                    huc.setReadTimeout(3000);
                    huc.setDoOutput(true);
                    huc.setUseCaches(false);
                    huc.setRequestMethod("POST");
                    String boundary = UUID.randomUUID().toString();
                    huc.setRequestProperty("Content-Type", "multi-part/form-data;boundary=" + boundary);

                    String endLine = "/r/n";
                    String prefix = "--";
                    StringBuilder sb = new StringBuilder();
                    sb.append(endLine);
                    sb.append(prefix + "boundary=" + boundary);
                    sb.append("Content-Disposition:form-data;name='file';filename=serve.html" + endLine);
                    sb.append(endLine);
                    InputStream is = serve.this.openFileInput("serve.html");
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    sb.append(endLine);
                    sb.append(endLine);
                    sb.append(prefix + "boundary=" + boundary + prefix);
                    br.close();

                    Log.d("xx", "sb====" + sb);
                    OutputStream os = huc.getOutputStream();
                    os.write(sb.toString().getBytes());
                    os.flush();
                    os.close();
                    InputStream result = huc.getInputStream();

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                // TODO Auto-generated method stub
                super.onProgressUpdate(values);
            }

        };
        ast.execute();
    }

    /**
     * 检查data/data目录是否存在html 不存在就把assets复制过来 * @throws IOException
     */
    private void isExistsHTML() {
        String path = getFilesDir().getAbsolutePath() + HTMLNAME; // data/data目录
        File file = new File(path);
        if (!file.exists() || true) {
            try {
                InputStream in = getAssets().open("serve.html"); // 从assets目录下复制
                FileOutputStream out = new FileOutputStream(file);
                int length = -1;
                byte[] buf = new byte[1024];
                while ((length = in.read(buf)) != -1) {
                    out.write(buf, 0, length);
                }
                out.flush();
                in.close();
                out.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * 保存从服务器获取的填充到html的数据
     *
     * @param str
     */
    private void saveHTMLData(String phon) {
        try {
            FileOutputStream out = this.openFileOutput(JSNAME, MODE_PRIVATE);
            byte[] bytes = phon.getBytes();
            out.write(bytes);
            out.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 获取填充到html的数据
     *
     * @param str
     */
    private String loadHTMLData() {
        String jsstr = null;
        try {
            FileInputStream in = this.openFileInput(JSNAME);
            byte[] bytes = new byte[256];
            in.read(bytes);
            in.close();
            jsstr = new String(bytes);
            return jsstr;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jsstr;
    }

}
