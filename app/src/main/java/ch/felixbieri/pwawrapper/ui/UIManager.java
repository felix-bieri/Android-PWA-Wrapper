package ch.felixbieri.pwawrapper.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.TypedValue;
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
            webView.loadUrl(Constants.getWebAppUrl());
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

    // set icon in recent activity view to a white one to be visible in the app bar
    public void changeRecentAppsIcon() {
        if (activity == null) {
            return;
        }

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = activity.getTheme();
        theme.resolveAttribute(R.color.colorPrimary, typedValue, true);
        int color = typedValue.data;

        // Using the Builder with setLabel() and setIcon() with resource ID:
        ActivityManager.TaskDescription description;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            description = new ActivityManager.TaskDescription.Builder()
                    .setLabel(activity.getString(R.string.app_name))
                    .setIcon(R.drawable.ic_appbar) // Corrected to use resource ID
                    .setBackgroundColor(color)
                    .build();
        } else {
            Bitmap iconWhite = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_appbar);

            description = new ActivityManager.TaskDescription(
                    activity.getResources().getString(R.string.app_name),
                    iconWhite,
                    color
            );
            activity.setTaskDescription(description);
            iconWhite.recycle();
        }

        activity.setTaskDescription(description);
    }
}
