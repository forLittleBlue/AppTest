package vpntest.xie.com.vpntest;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class WebViewAcitivity extends Activity {
    WebView mWebView;
    ProgressBar mProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view_acitivity);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.loadUrl("http://baidu.com");
        //启用支持javascript
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // TODO Auto-generated method stub
                if (newProgress == 100) {
                    // 网页加载完成
                    mWebView.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    // 加载中
                    mWebView.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.VISIBLE);
                }
                super.onProgressChanged(view, newProgress);
            }
        });

        //优先使用缓存
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        //不使用缓存：
//        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //改写物理按键——返回的逻辑
        if(keyCode==KeyEvent.KEYCODE_BACK) {
            if(mWebView.canGoBack()) {
                mWebView.goBack();//返回上一页面
                return true;
            } else  {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
