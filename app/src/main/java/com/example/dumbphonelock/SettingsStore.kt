package com.example.dumbphonelock

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import java.util.Calendar

object SettingsStore {
    private fun prefs(c: Context) = c.getSharedPreferences("dumbphone", Context.MODE_PRIVATE)
    fun start(c: Context) = prefs(c).getInt("start", 9 * 60)
    fun end(c: Context) = prefs(c).getInt("end", 17 * 60)
    fun days(c: Context) = prefs(c).getStringSet("days", setOf("2", "3", "4", "5", "6"))!!.toSet()
    fun allowed(c: Context) = prefs(c).getStringSet("allowed", emptySet())!!.toSet()
    fun enabled(c: Context) = prefs(c).getBoolean("enabled", false)
    fun pausedUntil(c: Context) = prefs(c).getLong("pausedUntil", 0L)
    fun pause(c: Context, durationMs: Long) = prefs(c).edit().putLong("pausedUntil", System.currentTimeMillis() + durationMs).apply()
    fun save(c: Context, start: Int, end: Int, days: Set<String>, allowed: Set<String>, enabled: Boolean) {
        prefs(c).edit().putInt("start", start).putInt("end", end).putStringSet("days", days.toSet())
            .putStringSet("allowed", allowed.toSet()).putBoolean("enabled", enabled).apply()
    }
    fun active(c: Context): Boolean {
        if (!enabled(c) || System.currentTimeMillis() < pausedUntil(c)) return false
        val now = Calendar.getInstance()
        val minute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val day = now.get(Calendar.DAY_OF_WEEK)
        val s = start(c); val e = end(c)
        if (s == e) return false // identical times mean no restriction
        val selected = days(c)
        if (s < e) return day.toString() in selected && minute in s until e
        // Overnight: the early-morning segment belongs to the preceding day.
        val previous = if (day == Calendar.SUNDAY) Calendar.SATURDAY else day - 1
        return (minute >= s && day.toString() in selected) || (minute < e && previous.toString() in selected)
    }
    fun protectedPackages(c: Context): Set<String> {
        val packages = mutableSetOf(c.packageName, "com.android.systemui", "com.android.settings")
        val pm = c.packageManager
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        pm.queryIntentActivities(home, PackageManager.MATCH_DEFAULT_ONLY).forEach { packages.add(it.activityInfo.packageName) }
        listOf(android.app.role.RoleManager.ROLE_DIALER, android.app.role.RoleManager.ROLE_SMS).forEach { role ->
            val rm = c.getSystemService(android.app.role.RoleManager::class.java)
            if (rm != null && rm.isRoleAvailable(role)) packages.addAll(rm.getRoleHolders(role))
        }
        // Emergency calling and core Android services should never be deliberately blocked.
        packages.addAll(listOf("com.android.phone", "com.android.server.telecom", "com.samsung.android.dialer", "com.google.android.dialer"))
        return packages
    }
}
