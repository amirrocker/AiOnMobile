package de.ams.techday.aionmobilemediapipe

import android.app.Application
import android.util.Log
import timber.log.Timber
import timber.log.Timber.DebugTree
import timber.log.Timber.Forest.plant
import timber.log.Timber.Tree

class AiOnMobileMediaPipeApplication() : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            plant(DebugTree())
        } else {
            plant(CrashReportingTree())
        }
    }

    /** A tree which logs important information for crash reporting.  */
    private class CrashReportingTree : Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return
            }

            Timber.log(priority, tag, message)

            if (t != null) {
                if (priority == Log.ERROR) {
                    Timber.e(t)
                } else if (priority == Log.WARN) {
                    Timber.w(t)
                }
            }
        }
    }
}