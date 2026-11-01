package com.samyak.netveil

import com.samyak.netveil.util.shellSplit
import org.junit.Assert.assertEquals
import org.junit.Test

class ArgumentsUtilsTest {

    @Test
    fun shellSplit_emptyAndWhitespace() {
        assertEquals(emptyList<String>(), shellSplit(""))
        assertEquals(emptyList<String>(), shellSplit("   "))
    }

    @Test
    fun shellSplit_simpleArguments() {
        val input = "--disorder 1 --proto http,https"
        val expected = listOf("--disorder", "1", "--proto", "http,https")
        assertEquals(expected, shellSplit(input))
    }

    @Test
    fun shellSplit_quotedArguments() {
        val input = "--split \"1 + s\" --fake -1"
        val expected = listOf("--split", "1 + s", "--fake", "-1")
        assertEquals(expected, shellSplit(input))
    }

    @Test
    fun shellSplit_singleQuotedArguments() {
        val input = "-s 1 -d 3 -t 8 -o 1 -K 't,h'"
        val expected = listOf("-s", "1", "-d", "3", "-t", "8", "-o", "1", "-K", "t,h")
        assertEquals(expected, shellSplit(input))
    }

    @Test
    fun shellSplit_escapedSpaces() {
        val input = "--custom\\ arg value"
        val expected = listOf("--custom arg", "value")
        assertEquals(expected, shellSplit(input))
    }
}

