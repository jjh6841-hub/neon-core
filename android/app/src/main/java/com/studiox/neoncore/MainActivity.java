package com.studiox.neoncore;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import androidx.activity.OnBackPressedCallback;
import androidx.core.app.NotificationCompat;
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

        @JavascriptInterface
        public void scheduleNotification(long delayMs, String title, String message) {
            Context ctx = getApplicationContext();
            Intent intent = new Intent(ctx, LabNotificationReceiver.class);
            intent.putExtra("title", title);
            intent.putExtra("message", message);
            PendingIntent pi = PendingIntent.getBroadcast(ctx, (int)(System.currentTimeMillis() % Integer.MAX_VALUE),
                intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayMs, pi);
        }
    }

    public static class LabNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("message");
            if (title == null) title = "NEON CORE";
            if (message == null) message = "연구가 완료되었습니다!";

            String channelId = "neoncore_lab";
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel ch = new NotificationChannel(channelId, "코어랩 연구", NotificationManager.IMPORTANCE_DEFAULT);
                ch.setDescription("코어랩 연구 완료 알림");
                nm.createNotificationChannel(ch);
            }
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            Intent openIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            if (openIntent != null) {
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                builder.setContentIntent(contentIntent);
            }
            nm.notify((int)(System.currentTimeMillis() % Integer.MAX_VALUE), builder.build());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        // Android 백 버튼 → JS handleBackButton() 즉시 호출 (Android 13+)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                webView.evaluateJavascript(
                    "if(window._handleBackButton) window._handleBackButton();", null);
            }
        });
    }

    // 풀스크린 (상태바 + 네비게이션바 숨김)
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
            WindowInsetsControllerCompat ctrl = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            ctrl.hide(WindowInsetsCompat.Type.systemBars());
            ctrl.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }

    // Android 12 이하: 기본 뒤로가기(webView.goBack) 방지 — 즉시 호출
    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        WebView webView = getBridge().getWebView();
        if (webView != null) {
            webView.evaluateJavascript(
                "if(window._handleBackButton) window._handleBackButton();", null);
        }
    }
}
