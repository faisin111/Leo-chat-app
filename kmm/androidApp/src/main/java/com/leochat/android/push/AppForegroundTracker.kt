package com.leochat.android.push

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.leochat.android.MainActivity
import java.util.concurrent.atomic.AtomicInteger

/** Tracks whether [MainActivity] is actively resumed in the foreground. */
object AppForegroundTracker : Application.ActivityLifecycleCallbacks {
    private val resumedMainCount = AtomicInteger(0)

    val isMainForeground: Boolean
        get() = resumedMainCount.get() > 0

    override fun onActivityResumed(activity: Activity) {
        if (activity is MainActivity) resumedMainCount.incrementAndGet()
    }

    override fun onActivityPaused(activity: Activity) {
        if (activity is MainActivity) resumedMainCount.updateAndGet { (it - 1).coerceAtLeast(0) }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
