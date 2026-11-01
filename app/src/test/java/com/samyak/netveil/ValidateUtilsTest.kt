package com.samyak.netveil

import com.samyak.netveil.util.checkIntRange
import com.samyak.netveil.util.checkIp
import com.samyak.netveil.util.checkNotLocalIp
import com.samyak.netveil.util.checkPort
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateUtilsTest {

    @Test
    fun checkIp_validIpv4() {
        assertTrue(checkIp("127.0.0.1"))
        assertTrue(checkIp("1.1.1.1"))
        assertTrue(checkIp("8.8.8.8"))
        assertTrue(checkIp("192.168.1.1"))
        assertTrue(checkIp("0.0.0.0"))
        assertTrue(checkIp("255.255.255.255"))
    }

    @Test
    fun checkIp_invalidIpv4() {
        assertFalse(checkIp(""))
        assertFalse(checkIp(" "))
        assertFalse(checkIp("256.0.0.1"))
        assertFalse(checkIp("1.2.3"))
        assertFalse(checkIp("1.2.3.4.5"))
        assertFalse(checkIp("abc"))
        assertFalse(checkIp("-1.0.0.1"))
    }

    @Test
    fun checkNotLocalIp_identifiesLocalAndRemote() {
        // Local IPs must be rejected
        assertFalse(checkNotLocalIp("127.0.0.1"))
        assertFalse(checkNotLocalIp("0.0.0.0"))

        // Remote IPs must be accepted
        assertTrue(checkNotLocalIp("1.1.1.1"))
        assertTrue(checkNotLocalIp("8.8.8.8"))
        assertTrue(checkNotLocalIp("9.9.9.9"))
    }

    @Test
    fun checkPort_validRange() {
        assertTrue(checkPort("1"))
        assertTrue(checkPort("80"))
        assertTrue(checkPort("443"))
        assertTrue(checkPort("1080"))
        assertTrue(checkPort("8080"))
        assertTrue(checkPort("65535"))
    }

    @Test
    fun checkPort_invalidRange() {
        assertFalse(checkPort("0"))
        assertFalse(checkPort("-1"))
        assertFalse(checkPort("65536"))
        assertFalse(checkPort("99999"))
        assertFalse(checkPort(""))
        assertFalse(checkPort("abc"))
    }

    @Test
    fun checkIntRange_withinRange() {
        assertTrue(checkIntRange("5", 0, 10))
        assertTrue(checkIntRange("0", 0, 10))
        assertTrue(checkIntRange("10", 0, 10))
        assertTrue(checkIntRange("-5", -10, 0))
    }

    @Test
    fun checkIntRange_outOfRange() {
        assertFalse(checkIntRange("11", 0, 10))
        assertFalse(checkIntRange("-1", 0, 10))
        assertFalse(checkIntRange("invalid", 0, 10))
        assertFalse(checkIntRange("", 0, 10))
    }
}

