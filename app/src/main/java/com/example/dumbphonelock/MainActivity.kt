package com.example.dumbphonelock

import android.app.Activity
import android.app.TimePickerDialog
import android.os.Bundle
import android.content.Intent
import android.provider.Settings
import android.content.pm.PackageManager
import android.view.ViewGroup
import android.widget.*

class MainActivity : Activity() {
    private fun time(m: Int) = "%02d:%02d".format(m / 60, m % 60)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(28, 32, 28, 32) }
        fun add(v: android.view.View) = root.addView(v, LinearLayout.LayoutParams(-1, -2))
        fun label(s: String, size: Float = 16f) = TextView(this).apply { text = s; textSize = size; setPadding(0, 12, 0, 10); add(this) }
        label("Dumbphone Lock", 26f)
        label("Prototype • Easy to turn off • No factory reset", 14f)
        var start = SettingsStore.start(this)
        var end = SettingsStore.end(this)
        val startButton = Button(this).apply { text = "Start: ${time(start)}" }; add(startButton)
        startButton.setOnClickListener { TimePickerDialog(this, { _, h, m -> start = h * 60 + m; startButton.text = "Start: ${time(start)}" }, start / 60, start % 60, false).show() }
        val endButton = Button(this).apply { text = "End: ${time(end)}" }; add(endButton)
        endButton.setOnClickListener { TimePickerDialog(this, { _, h, m -> end = h * 60 + m; endButton.text = "End: ${time(end)}" }, end / 60, end % 60, false).show() }
        label("Days to start lockdown (overnight periods finish next day):")
        val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val selectedDays = SettingsStore.days(this).toMutableSet()
        val dayRow = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }; add(dayRow)
        dayNames.forEachIndexed { i, name ->
            val key = (i + 1).toString()
            dayRow.addView(CheckBox(this).apply { text = name; isChecked = key in selectedDays; setOnCheckedChangeListener { _, checked -> if (checked) selectedDays.add(key) else selectedDays.remove(key) } })
        }
        label("Allowed apps (calls, SMS, launcher and Settings are always allowed):")
        val selectedApps = SettingsStore.allowed(this).toMutableSet()
        val appList = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }; add(appList)
        val pm = packageManager
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(launcher, 0)
            .distinctBy { it.activityInfo.packageName }
            .sortedBy { it.loadLabel(pm).toString().lowercase() }
        apps.forEach { info ->
            val pkg = info.activityInfo.packageName
            if (pkg !in SettingsStore.protectedPackages(this)) {
                appList.addView(CheckBox(this).apply {
                    text = info.loadLabel(pm).toString(); isChecked = pkg in selectedApps
                    setOnCheckedChangeListener { _, checked -> if (checked) selectedApps.add(pkg) else selectedApps.remove(pkg) }
                })
            }
        }
        val enabled = CheckBox(this).apply { text = "Enable scheduled lockdown"; isChecked = SettingsStore.enabled(this) }; add(enabled)
        val status = label("", 14f)
        add(Button(this).apply { text = "Save schedule"; setOnClickListener {
            SettingsStore.save(this@MainActivity, start, end, selectedDays, selectedApps, enabled.isChecked)
            status.text = "Saved. Lockdown ${if (SettingsStore.active(this@MainActivity)) "ACTIVE" else "not active right now"}."
        } })
        add(Button(this).apply { text = "Pause for 15 minutes"; setOnClickListener { SettingsStore.pause(this@MainActivity, 15 * 60 * 1000L); status.text = "Paused for 15 minutes." } })
        add(Button(this).apply { text = "Open Accessibility settings (enable / disable blocker)"; setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) } })
        label("To activate blocking, enable 'Dumbphone Lock app blocking' under Installed apps in Accessibility settings. You can disable it there at any time. No screen contents are read. Equal start/end times disable the schedule. Restrictions may be delayed by Samsung battery management.", 14f)
        setContentView(ScrollView(this).apply { addView(root) })
    }
}
