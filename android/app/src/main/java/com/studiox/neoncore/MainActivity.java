package com.studiox.neoncore;

import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import androidx.activity.OnBackPressedCallback;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    public class NeonBridge {
        @JavascriptInterface
        public void exitApp() {
            runOnUiThread(() -> {
                finishAffinity();
                System.exit(0);
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 풀스크린 (상태바 + 네비게이션바 숨김)
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat insetsCtrl = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        insetsCtrl.hide(WindowInsetsCompat.Type.systemBars());
        insetsCtrl.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        WebView webView = getBridge().getWebView();
        webView.addJavascriptInterface(new NeonBridge(), "NeonBridge");

        // 시스템 바 inset CSS 변수 주입 (풀스크린이므로 0px)
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

        // Android 백 버튼 → JS handleBackButton() 호출 (Android 13+ predictive back)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                webView.post(() -> webView.evaluateJavascript(
                    "if(window._handleBackButton) window._handleBackButton();", null));
            }
        });
    }

    // Android 12 이하: 기본 뒤로가기(webView.goBack) 방지
    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        WebView webView = getBridge().getWebView();
        if (webView != null) {
            webView.post(() -> webView.evaluateJavascript(
                "if(window._handleBackButton) window._handleBackButton();", null));
        }
    }
}
