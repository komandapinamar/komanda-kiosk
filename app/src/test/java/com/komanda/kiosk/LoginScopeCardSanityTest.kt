package com.komanda.kiosk

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LoginScopeCardSanityTest {

    @Test
    fun loginScreen_declaresPlatformScopeInfoCardWithWebGuidance() {
        val projectDir = File(System.getProperty("user.dir") ?: ".")
        val loginFile = File(projectDir, "src/main/java/com/komanda/kiosk/core/auth/ui/LoginScreen.kt")

        assertTrue("LoginScreen.kt file should exist", loginFile.exists())
        val content = loginFile.readText()

        assertTrue(
            "LoginScreen should reference the web backoffice login URL",
            content.contains("https://kda.ar/login")
        )
        assertTrue(
            "LoginScreen should state that the full backoffice is managed on web",
            content.contains("backoffice completo")
        )
    }
}
