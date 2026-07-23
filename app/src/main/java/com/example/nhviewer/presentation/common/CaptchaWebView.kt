package com.example.nhviewer.presentation.common

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.luminance

import androidx.compose.ui.res.stringResource
import com.example.nhviewer.R

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CaptchaDialog(
    siteKey: String,
    onSuccess: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val themeModeStr = if (isDark) "dark" else "light"

    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {
                    display: flex;
                    justify-content: center;
                    align-items: flex-start;
                    margin: 0;
                    padding: 0;
                    background-color: transparent;
                    overflow: hidden;
                }
                #turnstile-container {
                    margin-top: 25px;
                }
            </style>
        </head>
        <body>
            <div id="turnstile-container"></div>
            <script>
                function onTurnstileCallback(token) {
                    if (window.AndroidCaptcha) {
                        window.AndroidCaptcha.onSuccess(token);
                    }
                }
            </script>
            <script
                src="https://challenges.cloudflare.com/turnstile/v0/api.js?onload=onTurnstileLoaded&render=explicit"
                defer>
            </script>
            <script>
                function onTurnstileLoaded() {
                    turnstile.render('#turnstile-container', {
                        sitekey: '$siteKey',
                        callback: onTurnstileCallback,
                        theme: '$themeModeStr'
                    });
                }
            </script>
        </body>
        </html>
    """.trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.captcha_title)) },
        shape = MaterialTheme.shapes.extraLarge,
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(vertical = 8.dp)
            ) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
                            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.databaseEnabled = true

                            android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView,
                                    request: WebResourceRequest
                                ): Boolean {
                                    val host = request.url.host ?: ""
                                    return !host.endsWith("cloudflare.com")
                                }

                                override fun onReceivedSslError(
                                    view: WebView,
                                    handler: android.webkit.SslErrorHandler,
                                    error: android.net.http.SslError
                                ) {
                                    handler.proceed()
                                }
                            }
                            addJavascriptInterface(object {
                                @JavascriptInterface
                                fun onSuccess(token: String) {
                                    post { onSuccess(token) }
                                }
                            }, "AndroidCaptcha")
                            loadDataWithBaseURL(
                                "https://nhentai.net/",
                                htmlContent,
                                "text/html",
                                "UTF-8",
                                "https://nhentai.net/"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
