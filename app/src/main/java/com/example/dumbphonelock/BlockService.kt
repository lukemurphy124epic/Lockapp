package com.example.dumbphonelock

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.os.Handler
import android.os.Looper

class BlockService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private var currentPackage: String? = null
    private var lastBounce = 0L
    private val tick = object : Runnable {
        override fun run() {
            check(currentPackage)
            handler.postDelayed(this, 5000L)
        }
    }
    override fun onServiceConnected() {
        super.onServiceConnected()
        handler.removeCallbacks(tick)
        handler.post(tick)
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        currentPackage = pkg
        check(pkg)
    }
    private fun check(pkg: String?) {
        if (pkg == null || !SettingsStore.active(this)) return
        if (pkg in SettingsStore.protectedPackages(this) || pkg in SettingsStore.allowed(this)) return
        val now = System.currentTimeMillis()
        if (now - lastBounce < 1200L) return
        lastBounce = now
        performGlobalAction(GLOBAL_ACTION_HOME)
    }
    override fun onInterrupt() = Unit
    override fun onDestroy() { handler.removeCallbacks(tick); super.onDestroy() }
}
