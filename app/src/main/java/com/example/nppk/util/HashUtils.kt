package com.example.nppk.util

import java.security.MessageDigest

object HashUtils {
    /**
     * Возвращает SHA-256 хеш строки в виде Hex-строки.
     */
    fun sha256(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}
