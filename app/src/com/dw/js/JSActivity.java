package com.dw.js;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.dw.MainActivity;
import com.dw.R;


public class JSActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_js);
//        setContentView(MainActivity.mMove);
//        WebView web = MainActivity.mWebView;
        WebView web = (WebView)findViewById(R.id.web);
//		web.loadUrl("http://www.google.com");

        web.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // Activities and WebViews measure progress with different
                // scales.
                // The progress meter will automatically disappear when we reach
                // 100%
                JSActivity.this.setProgress(progress * 1000);
            }
        });
        web.setWebViewClient(new MyAndroidWebViewClient());
        web.addJavascriptInterface(new AndroidJavaScript(getApplicationContext()), "Hehe");

        web.loadUrl("https://account.sogou.com/connect/login?type=wap&provider=qq&client_id=2003&thirdInfo=&third_appid=");
//		web.loadUrl("https://www.baidu.com/");
//		web.loadUrl("file:///android_asset/QuestionAnswer.html");
        web.getSettings().setJavaScriptEnabled(true);

    }

    class MyAndroidWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.d("xx", "WebViewClient onPageStarted");
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.d("xx", "WebViewClient onPageFinished");
//            view.loadUrl("javascript:window.Hehe.getSource('<html>'+"
//                    + "document.getElementsByTagName('html')[0].innerHTML+'</html>');");
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("xx", "WebViewClient shouldOverrideUrlLoading");
            // TODO Auto-generated method stub
//            return super.shouldOverrideUrlLoading(view, url);
//	    	return true;
//	    	 view.loadUrl(url);
//	    	 return true;
//	    	 Intent intent = new Intent(JSActivity.this, serve.class);
//	    		startActivity(intent);
	    		return false;
//	    	 Intent intent = new Intent(Intent.ACTION_VIEW);
//	    		intent.setData(Uri.parse(url));
//	    		startActivity(intent);
//	    		return true;
        }


    }
}
