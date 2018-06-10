package com.extropies.www.eoshackathon;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable; 
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.SeekBar;

/**
 * Created by inst on 18-6-9.
 */

public class DiscoveryFragment extends Fragment {
    private WebView webView;
    private SeekBar webViewProgress;
    private String DiscoveryUrl = "http://www.chainb.com/?P=mhome";
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_discovery, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        webView = (WebView) this.getView().findViewById(R.id.discoveryWebview);
        webViewProgress = (SeekBar)this.getView().findViewById(R.id.webviewProgressBar);
        webViewProgress.setVisibility(View.GONE);

        webView.loadUrl(DiscoveryUrl);
        webViewInit();
    }

    @Override
    public void onDestroyView() {
        webView.clearCache(true);
        webView.clearHistory();
        super.onDestroyView();
    }

    public WebView getWebView() {
        return webView;
    }
    public static boolean onKeyDown(int keyCode, KeyEvent event, DiscoveryFragment fragment) {
        // TODO Auto-generated method stub
        if (keyCode == event.KEYCODE_BACK) {
            if (fragment.getWebView().canGoBack()) {
                fragment.getWebView().goBack();
                return true;
            }
        }
        return false;
    }

    public void webViewInit(){
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                System.out.println("============ shouldOverrideUrlLoading(WebView view, String url) :" + url);
                if (url.startsWith("intent://")){
                    return true;
                }
                view.loadUrl(url);
                webViewProgress.setVisibility(View.VISIBLE);
                return true;//
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                System.out.println("============ onLoadResource :" + url);
                super.onLoadResource(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                System.out.println("============ shouldOverrideUrlLoading :" + request.toString());
                return super.shouldOverrideUrlLoading(view, request);
            }



            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                System.out.println("============ shouldInterceptRequest :" + request.toString());
                return super.shouldInterceptRequest(view, request);

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

            }
        });
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                System.out.println("============ onProgressChanged :" + newProgress);
                webViewProgress.setProgress(newProgress);
                if (newProgress==100){
                    webViewProgress.setVisibility(View.GONE);
                }
            }
        });

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.getSettings().setAppCacheEnabled(false);

        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
    }
}
