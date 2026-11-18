package com.socialtrack.app

import android.app.Activity
import android.app.AlertDialog
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.*
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.app.usage.UsageStatsManager
import android.app.usage.UsageStats
import java.util.Calendar

class MainActivity : Activity() {
    private val bg = Color.rgb(11, 11, 18)
    private val panel = Color.rgb(25, 25, 38)
    private val purple = Color.rgb(167, 139, 250)
    private lateinit var content: LinearLayout
    private lateinit var totals: TextView
    private lateinit var detail: TextView
    private var popupShownThisInstance = false
    private val packages = linkedMapOf(
        "Instagram" to "com.instagram.android",
        "TikTok" to "com.zhiliaoapp.musically",
        "YouTube" to "com.google.android.youtube",
        "Facebook" to "com.facebook.katana"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        render()
    }

    override fun onResume() {
        super.onResume()
        if (::content.isInitialized) {
            refreshStats()
            if (!popupShownThisInstance) {
                popupShownThisInstance = true
                content.post { showActivityPopup() }
            }
        }
    }

    private fun showActivityPopup() {
        val usageGranted = hasUsageAccess()
        val totalUsage = if (usageGranted) packages.values.sumOf { getTodayUsage(it) } else 0L
        val totalVideos = packages.keys.sumOf { VideoCounterService.getCount(this, it) }

        val message = buildString {
            append("Total waktu penggunaan media sosial hari ini\\n")
            append(if (usageGranted) formatDuration(totalUsage) else "Belum tersedia — aktifkan Usage Access")
            append("\\n\\n")
            append("Estimasi video yang terdeteksi\\n")
            append("$totalVideos video")
            append("\\n\\n")
            packages.keys.forEach { name ->
                append("$name: ${VideoCounterService.getCount(this@MainActivity, name)} video\\n")
            }
            append("\\nJumlah video merupakan estimasi dari perubahan feed, bukan bukti video ditonton sampai selesai.")
        }

        AlertDialog.Builder(this)
            .setTitle("Ringkasan aktivitas SocialTrack")
            .setMessage(message)
            .setPositiveButton("Mengerti", null)
            .setNeutralButton("Pengaturan izin") { _, _ ->
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
            .show()
    }

    private fun hasUsageAccess(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return appOps.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), packageName) == AppOpsManager.MODE_ALLOWED
    }

    private fun render() {
        val scroll = ScrollView(this).apply { setBackgroundColor(bg) }
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 28, 22, 28)
        }
        scroll.addView(content)
        setContentView(scroll)

        text("SOCIALTRACK", 13, purple, true)
        text("Ringkasan aktivitas", 27, Color.WHITE, true)
        text("Pantau waktu layar dan estimasi video pendek.", 14, 0xFFAAAABC.toInt())
        space(20)

        val card = card()
        card.addView(label("TOTAL WAKTU MEDIA SOSIAL", 11, 0xFFB8B8C8.toInt()))
        totals = label("Memuat…", 32, Color.WHITE, true)
        card.addView(totals)
        detail = label("Instagram • TikTok • YouTube • Facebook", 12, 0xFFAAAABC.toInt())
        card.addView(detail)
        content.addView(card)
        space(14)

        text("Estimasi video ditonton", 18, Color.WHITE, true)
        val countCard = card()
        for ((name, _) in packages) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 9, 0, 9)
            }
            val nameView = label(name, 15, Color.WHITE, true)
            row.addView(nameView, LinearLayout.LayoutParams(0, -2, 1f))
            val value = label("${VideoCounterService.getCount(this, name)} video", 14, purple, true)
            value.tag = "count_$name"
            row.addView(value)
            countCard.addView(row)
        }
        content.addView(countCard)
        space(14)

        text("Waktu penggunaan hari ini", 18, Color.WHITE, true)
        val usageCard = card()
        for ((name, pkg) in packages) {
            val ms = getTodayUsage(pkg)
            usageCard.addView(label("$name  •  ${formatDuration(ms)}", 14, Color.WHITE))
            space(5, usageCard)
        }
        content.addView(usageCard)
        space(18)

        val usageButton = button("Aktifkan akses penggunaan aplikasi")
        usageButton.setOnClickListener { startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
        content.addView(usageButton)
        space(9)
        val accessibilityButton = button("Aktifkan penghitung video")
        accessibilityButton.setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        content.addView(accessibilityButton)
        space(10)
        text("Catatan: jumlah video adalah estimasi berdasarkan perubahan feed dan elemen layar. Akurasi dapat berbeda antarversi aplikasi. Statistik disimpan di perangkat.", 12, 0xFF9292A5.toInt())
        refreshStats()
    }

    private fun refreshStats() {
        if (!::totals.isInitialized) return
        if (!hasUsageAccess()) {
            totals.text = "Izin belum aktif"
            detail.text = "Aktifkan Usage Access agar durasi dapat dihitung."
        } else {
            var sum = 0L
            packages.values.forEach { sum += getTodayUsage(it) }
            totals.text = formatDuration(sum)
            detail.text = "Total hari ini • 4 aplikasi"
        }
        packages.keys.forEach { name ->
            content.findViewWithTag<TextView>("count_$name")?.text = "${VideoCounterService.getCount(this, name)} video"
        }
    }

    private fun getTodayUsage(pkg: String): Long {
        if (!hasUsageAccess()) return 0L
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val manager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val stats: List<UsageStats> = manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, cal.timeInMillis, System.currentTimeMillis())
        return stats.filter { it.packageName == pkg }.sumOf { it.totalTimeInForeground }
    }

    private fun formatDuration(ms: Long): String {
        val minutes = ms / 60000
        val h = minutes / 60
        val m = minutes % 60
        return if (h > 0) "${h}j ${m}m" else "${m} menit"
    }

    private fun card() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(17, 16, 17, 16)
        background = GradientDrawable().apply { setColor(panel); cornerRadius = 22f }
    }
    private fun text(s: String, size: Int, color: Int, bold: Boolean = false) {
        content.addView(label(s, size, color, bold))
        space(5)
    }
    private fun label(s: String, size: Int, color: Int, bold: Boolean = false) =
        TextView(this).apply {
            text = s; textSize = size.toFloat(); setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }
    private fun button(s: String) = Button(this).apply {
        text = s; isAllCaps = false; setTextColor(Color.WHITE)
        backgroundTintList = android.content.res.ColorStateList.valueOf(Color.rgb(110, 82, 190))
    }
    private fun space(h: Int, target: LinearLayout = content) {
        target.addView(View(this), LinearLayout.LayoutParams(1, h))
    }
}
