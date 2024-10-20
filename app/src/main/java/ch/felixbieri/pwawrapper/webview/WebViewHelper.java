package ch.felixbieri.pwawrapper.webview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.net.NetworkCapabilities;

import ch.felixbieri.pwawrapper.R;
import ch.felixbieri.pwawrapper.Constants;
import ch.felixbieri.pwawrapper.ui.UIManager;

public class WebViewHelper {
    // Instance variables
    private Activity activity;
    private UIManager uiManager;
    private WebView webView;
    private WebSettings webSettings;

    public WebViewHelper(Activity activity, UIManager uiManager) {
        this.activity = activity;
        this.uiManager = uiManager;
        this.webView = activity.findViewById(R.id.webView);
        this.webSettings = webView.getSettings();
    }

    /**
     * Checks if the device has an active network connection with internet capability.
     * Note: This method does not guarantee a functional internet connection.
     * It only verifies that the network reports having internet access.
     *
     * @return True if the device has an active network connection that claims to have internet capability,
     *         false otherwise.
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkCapabilities capabilities = manager.getNetworkCapabilities(manager.getActiveNetwork());
            return capabilities != null &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        }
        return false;
    }

    // manipulate cache settings to make sure our PWA gets updated
    private void useCache(Boolean use) {
        if (use) {
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        } else {
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        }
    }

    // public method changing cache settings according to network availability.
    // retrieve content from cache primarily if not connected,
    // allow fetching from web too otherwise to get updates.
    public void forceCacheIfOffline() {
        useCache(!isNetworkAvailable());
    }

    // handles initial setup of webview
    @SuppressLint("SetJavaScriptEnabled")
    public void setupWebView() {
        // accept cookies
        CookieManager.getInstance().setAcceptCookie(true);
        // enable JS
        webSettings.setJavaScriptEnabled(true);
        // must be set for our js-popup-blocker:
        webSettings.setSupportMultipleWindows(true);

        // PWA settings
        webSettings.setDomStorageEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDatabaseEnabled(true);

        // enable mixed content mode conditionally
        if (Constants.ENABLE_MIXED_CONTENT) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }

        // retrieve content from cache primarily if not connected
        forceCacheIfOffline();

        // set User Agent
        if (Constants.OVERRIDE_USER_AGENT || Constants.POSTFIX_USER_AGENT) {
            String userAgent = webSettings.getUserAgentString();
            if (Constants.OVERRIDE_USER_AGENT) {
                userAgent = Constants.USER_AGENT;
            }
            if (Constants.POSTFIX_USER_AGENT) {
                userAgent = userAgent + " " + Constants.USER_AGENT_POSTFIX;
            }
            webSettings.setUserAgentString(userAgent);
        }

        // enable HTML5-support
        webView.setWebChromeClient(new WebChromeClient() {
            //simple yet effective redirect/popup blocker
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                Message href = view.getHandler().obtainMessage();
                view.requestFocusNodeHref(href);
                final String popupUrl = href.getData().getString("url");
                if (popupUrl != null) {
                    //it's null for most rouge browser hijack ads
                    webView.loadUrl(popupUrl);
                    return true;
                }
                return false;
            }

            // update ProgressBar
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                uiManager.setLoadingProgress(newProgress);
                super.onProgressChanged(view, newProgress);
            }
        });

        // Set up Webview client
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                handleUrlLoad(view, url);
            }

            // handle loading error by showing the offline screen
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    int errorCode = error.getErrorCode();
                    if (errorCode == WebViewClient.ERROR_CONNECT) {
                        // Show "No internet connection" message
                    } else {
                        handleLoadError(errorCode);
                    }
                }
            }
        });
    }

    // Lifecycle callbacks
    public void onPause() {
        webView.onPause();
    }

    public void onResume() {
        webView.onResume();
    }

    // show "no app found" dialog
    private void showNoAppDialog(Activity thisActivity) {
        new AlertDialog.Builder(thisActivity)
            .setTitle(R.string.noapp_heading)
            .setMessage(R.string.noapp_description)
            .show();
    }

    // handle load errors
    private void handleLoadError(int errorCode) {
        if (errorCode != WebViewClient.ERROR_UNSUPPORTED_SCHEME) {
            uiManager.setOffline(true);
        } else {
            // Unsupported Scheme, recover
            new Handler(Looper.getMainLooper()).postDelayed(() -> loadIntentUrl(Constants.WEBAPP_URL), 100);
        }
    }

    // handle external urls
    @SuppressLint("QueryPermissionsNeeded")
    private void handleUrlLoad(WebView view, String url) {
        // prevent loading content that isn't ours

        if (url.startsWith(Constants.WEBAPP_URL) || url.startsWith(Constants.WEB_URL)) {
            // let WebView load the page!
            // activate loading animation screen
            uiManager.setLoading(true);
            return; // Allow WebView to load
        }

        // Handle external URL
        //view.stopLoading();
        //view.reload();

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            String intentUrl = Uri.parse(url).toString();

            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                // Use this if the app is clear
                activity.startActivity(intent);
                view.goBack();

            } else if (intentUrl.contains("maps.google.com") || intentUrl.contains("maps")) {
                // Use this for maps because the consent page throws ACTION_VIEW exception
                url = intentUrl.replace("https://consent.google.com/m?continue=", "");
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                activity.startActivity(intent);
                view.goBack();

            } else if (intentUrl.contains("https")) {
                activity.startActivity(intent);
                view.goBack();
            } else {
                showNoAppDialog(activity);
                webView.loadUrl(Constants.WEBAPP_URL);
            }
        } catch (Exception e) {
            showNoAppDialog(activity);
            webView.loadUrl(Constants.WEBAPP_URL);
        }
    }

    // handle back button press
    public boolean goBack() {
        if (webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return false;
    }

    // load app startpage
    public void loadHome() {
        webView.loadUrl(Constants.WEBAPP_URL);
    }

    // load URL from intent
    public void loadIntentUrl(String url) {
        if (url.contains(Constants.WEBAPP_HOST)) {
            webView.loadUrl(url);
        } else {
            // Fallback
            loadHome();
        }
    }
}
