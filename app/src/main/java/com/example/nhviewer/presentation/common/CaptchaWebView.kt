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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CaptchaDialog(
    siteKey: String,
    onSuccess: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // 使用 Turnstile 官方的显式渲染方式：
    // 1. 先定义全局回调函数 onTurnstileCallback，再通过 onload 参数告知 Turnstile
    // 2. 避免 async/defer 引起的 window.onload 时 turnstile 对象未就绪问题
    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    height: 100vh;
                    margin: 0;
                    background-color: #121212;
                    color: #ffffff;
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
                        theme: 'dark'
                    });
                }
            </script>
        </body>
        </html>
    """.trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("人机验证") },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            // 拦截所有导航，防止 WebView 跳转到外部页面
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView,
                                    request: WebResourceRequest
                                ): Boolean {
                                    // 只允许 challenges.cloudflare.com 及内嵌资源通过，其余全部拦截
                                    val host = request.url.host ?: ""
                                    return !host.endsWith("cloudflare.com")
                                }
                            }
                            addJavascriptInterface(object {
                                @JavascriptInterface
                                fun onSuccess(token: String) {
                                    post { onSuccess(token) }
                                }
                            }, "AndroidCaptcha")
                            // historyUrl 指定为 baseUrl，防止 null 导致回退导航异常
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
                Text("取消")
            }
        }
    )
}
