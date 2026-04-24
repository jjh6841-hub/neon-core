package com.studiox.neoncore;

import android.os.Bundle;
import android.webkit.WebView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebView webView = getBridge().getWebView();
        ViewCompat.setOnApplyWindowInsetsListener(webView, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            float density = getResources().getDisplayMetrics().density;
            int top = Math.round(bars.top / density);
            int bottom = Math.round(bars.bottom / density);
            int left = Math.round(bars.left / density);
            int right = Math.round(bars.right / density);
            String js = "document.documentElement.style.setProperty('--sat','" + top + "px');"
                       + "document.documentElement.style.setProperty('--sab','" + bottom + "px');"
                       + "document.documentElement.style.setProperty('--sal','" + left + "px');"
                       + "document.documentElement.style.setProperty('--sar','" + right + "px');";
            webView.post(() -> webView.evaluateJavascript(js, null));
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
