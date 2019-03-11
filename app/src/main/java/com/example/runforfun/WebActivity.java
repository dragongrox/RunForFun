package com.example.runforfun;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebActivity extends AppCompatActivity {
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        webView = findViewById(R.id.webView);

        String URLdestino = "https://dragongroxtime.wordpress.com/";
        //Configuramos el objeto WebView
        webView.setWebViewClient(new WebViewClient());
        //le indicamos que url  tiene que cargar
        webView.loadUrl(URLdestino);
        //no olvidarse de solicitar permiso en AndroidManifest.

    }
}
