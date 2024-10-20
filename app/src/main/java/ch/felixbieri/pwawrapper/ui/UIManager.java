package ch.felixbieri.pwawrapper.ui;

import android.app.Activity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import ch.felixbieri.pwawrapper.Constants;
import ch.felixbieri.pwawrapper.R;

public class UIManager {
    // Instance variables
    private final Activity activity;
    private final WebView webView;
    private final ProgressBar progressSpinner;
    private final ProgressBar progressBar;
    private final LinearLayout offlineContainer;
    private boolean pageLoaded = false;

    public UIManager(Activity activity) {
        this.activity = activity;
        this.progressBar = activity.findViewById(R.id.progressBarBottom);
        this.progressSpinner = activity.findViewById(R.id.progressSpinner);
        this.offlineContainer = activity.findViewById(R.id.offlineContainer);
        this.webView = activity.findViewById(R.id.webView);

        // set click listener for offline-screen
        offlineContainer.setOnClickListener(v -> {
            webView.loadUrl(Constants.WEBAPP_URL);
            setOffline(false);
        });
    }

    // Set Loading Progress for ProgressBar
    public void setLoadingProgress(int progress) {
        // set progress in UI
        progressBar.setProgress(progress, true);

        // hide ProgressBar if not applicable
        if (progress >= 0 && progress < 100) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
        }

        // get app screen back if loading is almost complete
        if (progress >= Constants.PROGRESS_THRESHOLD && !pageLoaded) {
            setLoading(false);
        }
    }

    // Show loading animation screen while app is loading/caching the first time
    public void setLoading(boolean isLoading) {
        if (isLoading) {
            progressSpinner.setVisibility(View.VISIBLE);
            webView.animate().translationY(Constants.SLIDE_EFFECT).alpha(0.5F).setInterpolator(new AccelerateInterpolator()).start();
        } else {
            webView.setTranslationY(Constants.SLIDE_EFFECT);
            webView.animate().translationY(0).alpha(1F).setInterpolator(new DecelerateInterpolator()).start();
            progressSpinner.setVisibility(View.INVISIBLE);
        }
        pageLoaded = !isLoading;
    }

    // handle visibility of offline screen
    public void setOffline(boolean offline) {
        if (offline) {
            setLoadingProgress(100);
            webView.setVisibility(View.INVISIBLE);
            offlineContainer.setVisibility(View.VISIBLE);
        } else {
            webView.setVisibility(View.VISIBLE);
            offlineContainer.setVisibility(View.INVISIBLE);
        }
    }
}
