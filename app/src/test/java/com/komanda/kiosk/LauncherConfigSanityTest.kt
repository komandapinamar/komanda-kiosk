package com.komanda.kiosk

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LauncherConfigSanityTest {

    @Test
    fun manifest_declaresSingleTaskAndEnabledHomeLauncherAlias() {
        val projectDir = File(System.getProperty("user.dir") ?: ".")
        val manifestFile = File(projectDir, "src/main/AndroidManifest.xml")

        assertTrue("AndroidManifest.xml should exist", manifestFile.exists())
        val manifestContent = manifestFile.readText()

        assertTrue(
            "KioskActivity must declare android:launchMode=\"singleTask\"",
            manifestContent.contains("android:launchMode=\"singleTask\"")
        )

        assertTrue(
            "KioskHomeLauncherAlias must be enabled by default (android:enabled=\"true\")",
            manifestContent.contains("android:name=\".KioskHomeLauncherAlias\"") &&
                    manifestContent.contains("android:enabled=\"true\"")
        )

        assertTrue(
            "KioskHomeLauncherAlias must declare CATEGORY_HOME",
            manifestContent.contains("android.intent.category.HOME")
        )
    }

    @Test
    fun kioskActivity_invokesLauncherManagerToLockTask() {
        val projectDir = File(System.getProperty("user.dir") ?: ".")
        val activityFile = File(projectDir, "src/main/java/com/komanda/kiosk/ui/KioskActivity.kt")

        assertTrue("KioskActivity.kt should exist", activityFile.exists())
        val activityContent = activityFile.readText()

        assertTrue(
            "KioskActivity should invoke startKioskLockTask",
            activityContent.contains("startKioskLockTask")
        )
    }
}
