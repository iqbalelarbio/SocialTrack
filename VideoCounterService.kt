package com.socialtrack.app

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.content.Context
import android.os.SystemClock

class VideoCounterService : AccessibilityService() {
    private var lastCountAt = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val pkg = event.packageName?.toString() ?: return
        val platform = when (pkg) {
            "com.instagram.android" -> "Instagram"
            "com.zhiliaoapp.musically" -> "TikTok"
            "com.google.android.youtube" -> "YouTube"
            "com.facebook.katana" -> "Facebook"
            else -> return
        }
        if (event.eventType != AccessibilityEvent.TYPE_VIEW_SCROLLED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) return

        val now = SystemClock.elapsedRealtime()
        if (now - lastCountAt < 1400) return

        val root = rootInActiveWindow ?: return
        if (!looksLikeShortVideoFeed(root, platform)) return

        lastCountAt = now
        val prefs = getSharedPreferences("video_counts", Context.MODE_PRIVATE)
        val key = "count_$platform"
        prefs.edit().putInt(key, prefs.getInt(key, 0) + 1).apply()
    }

    private fun looksLikeShortVideoFeed(root: AccessibilityNodeInfo, platform: String): Boolean {
        val clues = when (platform) {
            "Instagram" -> listOf("reel", "reels", "audio", "like", "comment")
            "TikTok" -> listOf("for you", "following", "share", "comment", "sound")
            "YouTube" -> listOf("shorts", "subscribe", "like", "comments")
            else -> listOf("reel", "reels", "comment", "share", "like")
        }
        var matches = 0
        val stack = ArrayDeque<AccessibilityNodeInfo>()
        stack.add(root)
        var visited = 0
        while (stack.isNotEmpty() && visited < 180) {
            val node = stack.removeLast()
            visited++
            val description = (node.contentDescription?.toString() ?: "").lowercase()
            val text = (node.text?.toString() ?: "").lowercase()
            if (clues.any { description.contains(it) || text.contains(it) }) matches++
            for (i in 0 until node.childCount) node.getChild(i)?.let { stack.add(it) }
        }
        return matches >= 2
    }

    override fun onInterrupt() {}

    companion object {
        fun getCount(context: Context, platform: String): Int =
            context.getSharedPreferences("video_counts", Context.MODE_PRIVATE).getInt("count_$platform", 0)
    }
}
