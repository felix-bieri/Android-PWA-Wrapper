package ch.felixbieri.pwawrapper;

import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.window.SplashScreen;

import ch.felixbieri.pwawrapper.ui.UIManager;
import ch.felixbieri.pwawrapper.webview.WebViewHelper;

public class MainActivity extends AppCompatActivity {
    private UIManager uiManager;
    private WebViewHelper webViewHelper;
    private boolean intentHandled = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        // Setup Theme
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme_NoActionBar);
        setContentView(R.layout.activity_main);

        // Setup Helpers
        // Globals
        UIManager uiManager = new UIManager(this);
        webViewHelper = new WebViewHelper(this, uiManager);

        // Setup App
        webViewHelper.setupWebView();
        uiManager.changeRecentAppsIcon();

        // Check for Intents
        try {
            String intentAction = getIntent().getAction();
            if (!intentHandled && intentAction != null && intentAction.equals(Intent.ACTION_VIEW)) {
                Uri intentUri = getIntent().getData();
                if (intentUri != null) {
                    if ("geo".equals(intentUri.getScheme())) {
                        Intent mapsIntent = new Intent(Intent.ACTION_VIEW, intentUri);
                        if (mapsIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(mapsIntent);
                            intentHandled = true;
                        }
                    } else if ("mailto".equals(intentUri.getScheme())) {
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, intentUri);
                        if (emailIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(emailIntent);
                            intentHandled = true;
                        }
                    } else {
                        webViewHelper.loadHome();
                    }
                }
            } else {
                webViewHelper.loadHome();
            }
        } catch (Exception e) {
            webViewHelper.loadHome();
        }

        //Splash Screen
        SplashScreen splashScreen = getSplashScreen();

        splashScreen.setOnExitAnimationListener(splashScreenViewProvider -> {

            // Simulate a long-running task
            // This is where you would normally do your work
            // Call remove() when you're done
            new Handler(Looper.getMainLooper()).postDelayed(splashScreenViewProvider::remove, 500); // Example: Hide splash screen after 0.5 seconds

        });
    }

    @Override
    protected void onPause() {
        webViewHelper.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        webViewHelper.onResume();
        // retrieve content from cache primarily if not connected
        webViewHelper.forceCacheIfOffline();
        super.onResume();
    }

    // Handle back-press in browser
    @Override
    public void onBackPressed() {
        if (!webViewHelper.goBack()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                finish();
            } else {
                super.onBackPressed();
            }
        }
    }
}
