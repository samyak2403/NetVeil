package com.samyak.netveil

import com.samyak.netveil.core.NetVeilProxyCmdPreferences
import com.samyak.netveil.core.NetVeilProxyUIPreferences
import com.samyak.netveil.model.*
import com.samyak.netveil.service.*
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelsAndStatusTest {

    @Test
    fun testDesyncMethod_ordinalsMatchNativeC() {
        // Must strictly match native DESYNC_METHODS order in native-lib.c:
        // DESYNC_NONE, DESYNC_SPLIT, DESYNC_DISORDER, DESYNC_FAKE, DESYNC_OOB, DESYNC_DISOOB
        assertEquals(0, DesyncMethod.None.ordinal)
        assertEquals(1, DesyncMethod.Split.ordinal)
        assertEquals(2, DesyncMethod.Disorder.ordinal)
        assertEquals(3, DesyncMethod.Fake.ordinal)
        assertEquals(4, DesyncMethod.OOB.ordinal)
        assertEquals(5, DesyncMethod.DISOOB.ordinal)
    }

    @Test
    fun testHostsMode_ordinalsMatchNativeC() {
        // Must match native hosts_mode in native-lib.c:
        // HOSTS_DISABLE, HOSTS_BLACKLIST, HOSTS_WHITELIST
        assertEquals(0, HostsMode.Disable.ordinal)
        assertEquals(1, HostsMode.Blacklist.ordinal)
        assertEquals(2, HostsMode.Whitelist.ordinal)
    }

    @Test
    fun testStatusTransitions() {
        setStatus(AppStatus.Running, Mode.VPN)
        assertEquals(AppStatus.Running, appStatus.first)
        assertEquals(Mode.VPN, appStatus.second)
        assertEquals(AppStatus.Running, statusFlow.value.status)

        updateTrafficStats(TrafficStats(txPackets = 10, txBytes = 1024, rxPackets = 8, rxBytes = 2048))
        assertEquals(10L, statusFlow.value.stats.txPackets)
        assertEquals(1024L, statusFlow.value.stats.txBytes)
        assertEquals(8L, statusFlow.value.stats.rxPackets)
        assertEquals(2048L, statusFlow.value.stats.rxBytes)

        setStatus(AppStatus.Halted, Mode.VPN)
        assertEquals(AppStatus.Halted, appStatus.first)
        assertEquals(0L, statusFlow.value.stats.txBytes) // stats reset on Halted
    }

    @Test
    fun testAppThemeMode_displayNames() {
        assertEquals("Follow System", AppThemeMode.System.displayName)
        assertEquals("Light", AppThemeMode.Light.displayName)
        assertEquals("Dark", AppThemeMode.Dark.displayName)
    }

    @Test
    fun testMode_titles() {
        assertEquals("VPN", Mode.VPN.title)
        assertEquals("Proxy", Mode.Proxy.title)
    }

    @Test
    fun testNetVeilProxyUIPreferences_defaults() {
        val uiPrefs = NetVeilProxyUIPreferences()
        assertEquals("127.0.0.1", uiPrefs.ip)
        assertEquals(1080, uiPrefs.port)
        assertEquals(512, uiPrefs.maxConnections)
        assertEquals(16384, uiPrefs.bufferSize)
        assertEquals(DesyncMethod.Disorder, uiPrefs.desyncMethod)
        assertEquals(1, uiPrefs.splitPosition)
        assertEquals(8, uiPrefs.fakeTtl)
        assertEquals("www.iana.org", uiPrefs.fakeSni)
        assertTrue(uiPrefs.desyncHttp)
        assertTrue(uiPrefs.desyncHttps)
        assertFalse(uiPrefs.desyncUdp)
        assertFalse(uiPrefs.splitAtHost)
    }

    @Test
    fun testNetVeilProxyCmdPreferences_formatting() {
        val cmdPrefs = NetVeilProxyCmdPreferences("--disorder 1 --proto http,https")
        assertArrayEquals(
            arrayOf("ciadpi", "--disorder", "1", "--proto", "http,https"),
            cmdPrefs.args
        )
    }
}

