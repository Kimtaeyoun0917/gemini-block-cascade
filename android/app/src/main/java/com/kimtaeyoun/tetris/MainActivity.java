package com.kimtaeyoun.tetris;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.view.DisplayCutout;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

/**
 * 모바일 테트리스 — 저장소의 tetris/ 웹앱(index.html·JS·CSS·아이콘)을
 * 앱 assets/www 안에 그대로 담아 오프라인에서도 실행하는 WebView 앱.
 */
public class MainActivity extends Activity {

    private static final String START_URL = "file:///android_asset/www/index.html";
    private static final int BG_COLOR = 0xFF070B14;

    private WebView webView;
    private int lastTopInset = -1;
    private int lastBottomInset = -1;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 게임 중 화면이 꺼지지 않게 한다.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        webView = new WebView(this);
        webView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        webView.setBackgroundColor(BG_COLOR);
        webView.setFitsSystemWindows(false);
        configureWebView(webView);

        setContentView(webView);
        applyEdgeToEdge();
        hideSystemBars();
        webView.loadUrl(START_URL);
    }

    private void configureWebView(WebView view) {
        WebSettings s = view.getSettings();

        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);          // 최고 점수 localStorage 저장
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setAllowFileAccessFromFileURLs(true);
        s.setAllowUniversalAccessFromFileURLs(true);

        // 세로·터치 게임이므로 확대/축소와 스크롤을 막는다.
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setTextZoom(100);
        s.setSupportMultipleWindows(false);
        s.setJavaScriptCanOpenWindowsAutomatically(false);

        // 오프라인 실행: assets 에서 바로 읽는다.
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            s.setSafeBrowsingEnabled(false);
        }

        view.setVerticalScrollBarEnabled(false);
        view.setHorizontalScrollBarEnabled(false);
        view.setOverScrollMode(View.OVER_SCROLL_NEVER);
        view.setLongClickable(false);
        view.setHapticFeedbackEnabled(false);
        view.setSoundEffectsEnabled(false);
        view.setOnLongClickListener(v -> true);

        view.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest request) {
                // 앱 밖으로 나가는 이동은 없다. 게임 안에서만 머문다.
                return true;
            }
        });

        // 화면 노치·컷아웃과 제스처 영역만큼 안쪽으로 밀어 버튼이 가려지지 않게 한다.
        view.setOnApplyWindowInsetsListener((v, insets) -> {
            int[] pads = computeSafeInsets(insets);
            if (pads[0] != lastTopInset || pads[1] != lastBottomInset) {
                lastTopInset = pads[0];
                lastBottomInset = pads[1];
                v.setPadding(0, pads[0], 0, pads[1]);
            }
            return insets;
        });
    }

    private int[] computeSafeInsets(WindowInsets insets) {
        int top = 0;
        int bottom = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            DisplayCutout cutout = insets.getDisplayCutout();
            if (cutout != null) {
                top = Math.max(top, cutout.getSafeInsetTop());
                bottom = Math.max(bottom, cutout.getSafeInsetBottom());
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Insets gestures = insets.getInsets(WindowInsets.Type.mandatorySystemGestures());
            bottom = Math.max(bottom, gestures.bottom);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Insets gestures = insets.getMandatorySystemGestureInsets();
            bottom = Math.max(bottom, gestures.bottom);
        }

        return new int[] { top, bottom };
    }

    private void applyEdgeToEdge() {
        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            window.setAttributes(lp);
        }
    }

    private void hideSystemBars() {
        Window window = getWindow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            }
        } else {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // 사용자가 바를 잠깐 끌어낸 뒤 다시 전체 화면으로 돌아오게 한다.
            hideSystemBars();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.setWebViewClient(null);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
