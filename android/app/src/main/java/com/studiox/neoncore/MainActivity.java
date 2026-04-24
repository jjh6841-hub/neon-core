package com.studiox.neoncore;

import android.os.Bundle;
import androidx.core.view.WindowCompat;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Android 15 edge-to-edge 강제 해제 → 시스템 바와 콘텐츠 겹침 방지
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
    }
}
