package com.segarstok.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class UrgencyLevelTest {

    @Test
    fun testFromDaysLeft_calculatesCorrectUrgencyLevel() {
        assertEquals(UrgencyLevel.EXPIRED, UrgencyLevel.fromDaysLeft(-5))
        assertEquals(UrgencyLevel.EXPIRED, UrgencyLevel.fromDaysLeft(-1))
        assertEquals(UrgencyLevel.DUE_TODAY, UrgencyLevel.fromDaysLeft(0))
        assertEquals(UrgencyLevel.URGENT, UrgencyLevel.fromDaysLeft(1))
        assertEquals(UrgencyLevel.URGENT, UrgencyLevel.fromDaysLeft(3))
        assertEquals(UrgencyLevel.NEAR, UrgencyLevel.fromDaysLeft(4))
        assertEquals(UrgencyLevel.NEAR, UrgencyLevel.fromDaysLeft(7))
        assertEquals(UrgencyLevel.ATTENTION, UrgencyLevel.fromDaysLeft(8))
        assertEquals(UrgencyLevel.ATTENTION, UrgencyLevel.fromDaysLeft(14))
        assertEquals(UrgencyLevel.NORMAL, UrgencyLevel.fromDaysLeft(15))
        assertEquals(UrgencyLevel.NORMAL, UrgencyLevel.fromDaysLeft(30))
    }
}
