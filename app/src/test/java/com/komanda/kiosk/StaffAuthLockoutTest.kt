package com.komanda.kiosk

import com.komanda.kiosk.ui.EspressoManager
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StaffAuthLockoutTest {

    private val api = FakeEspressoApi()
    private val manager = EspressoManager(
        tenantId = "tenant-test-1",
        tenantName = "Kiosk Test",
        api = api
    )

    @Before
    fun setUp() {
        manager.resetStaffLockoutForTesting()
        api.mockVerifyStaffSuccess = true
    }

    @Test
    fun verifyStaff_validCredentials_succeeds() = runTest {
        val success = manager.verifyStaffCredentials("admin@test.com", "Password123!")
        assertTrue("Valid credentials should succeed", success)
        assertFalse("Staff should not be locked out", manager.isStaffLockedOut)
    }

    @Test
    fun verifyStaff_threeFailedAttempts_triggersFiveMinuteLockout() = runTest {
        api.mockVerifyStaffSuccess = false

        // Attempt 1
        val res1 = manager.verifyStaffCredentials("wrong@test.com", "bad1")
        assertFalse(res1)
        assertFalse(manager.isStaffLockedOut)

        // Attempt 2
        val res2 = manager.verifyStaffCredentials("wrong@test.com", "bad2")
        assertFalse(res2)
        assertFalse(manager.isStaffLockedOut)

        // Attempt 3: Triggers lockout
        val res3 = manager.verifyStaffCredentials("wrong@test.com", "bad3")
        assertFalse(res3)
        assertTrue("Staff should now be locked out after 3 failures", manager.isStaffLockedOut)
        assertTrue("Remaining lockout seconds should be > 200", manager.remainingLockoutSeconds > 200)

        // 4th attempt while locked out: immediately rejected without API call
        api.mockVerifyStaffSuccess = true // Even if credentials were suddenly right
        val res4 = manager.verifyStaffCredentials("admin@test.com", "Password123!")
        assertFalse("Attempt while locked out must be blocked", res4)
        assertTrue(manager.isStaffLockedOut)
    }
}
