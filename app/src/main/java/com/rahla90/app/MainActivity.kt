package com.rahla90.app

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import org.json.JSONObject

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private var pendingBackup: String? = null

    inner class BackupBridge {
        @JavascriptInterface
        fun saveBackup(json: String) {
            pendingBackup = json
            runOnUiThread {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                    putExtra(Intent.EXTRA_TITLE, "rahla90-backup.json")
                }
                startActivityForResult(intent, REQUEST_SAVE_BACKUP)
            }
        }

        @JavascriptInterface
        fun pickBackup() {
            runOnUiThread {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                }
                startActivityForResult(intent, REQUEST_OPEN_BACKUP)
            }
        }
    }

    @Suppress("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(14, 79, 134)
        window.navigationBarColor = Color.rgb(14, 79, 134)

        webView = WebView(this).apply {
            setBackgroundColor(Color.rgb(243, 247, 251))
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
                allowFileAccess = true
                allowContentAccess = true
                builtInZoomControls = false
                displayZoomControls = false
            }
            addJavascriptInterface(BackupBridge(), "AndroidBridge")
        }
        setContentView(webView)
        webView.loadUrl("file:///android_asset/index.html")
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (resultCode != RESULT_OK || resultData?.data == null) return

        val uri: Uri = resultData.data!!
        try {
            when (requestCode) {
                REQUEST_SAVE_BACKUP -> {
                    contentResolver.openOutputStream(uri)?.use {
                        it.write((pendingBackup ?: "{}").toByteArray(Charsets.UTF_8))
                    }
                    pendingBackup = null
                    Toast.makeText(this, "تم حفظ النسخة الاحتياطية", Toast.LENGTH_SHORT).show()
                }
                REQUEST_OPEN_BACKUP -> {
                    val json = contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: "{}"
                    webView.evaluateJavascript("window.importBackupText(${JSONObject.quote(json)})", null)
                }
            }
        } catch (_: Exception) {
            Toast.makeText(this, "تعذر التعامل مع ملف النسخة الاحتياطية", Toast.LENGTH_LONG).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }

    companion object {
        private const val REQUEST_SAVE_BACKUP = 2001
        private const val REQUEST_OPEN_BACKUP = 2002
    }
}
