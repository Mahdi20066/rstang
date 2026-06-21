package com.v2ray.ang.rsta

import com.v2ray.ang.AppConfig
import com.v2ray.ang.handler.MmkvManager

/**
 * Settings + defaults for the RSTA Spoof local SNI-spoof proxy.
 *
 * These defaults intentionally mirror `defaultConfig` in rstaspoofV5.0.0.go
 * (CONNECT_IP, CONNECT_PORT, FAKE_SNI, BYPASS_METHOD), since the bundled
 * native engine (libsnispf.so) was built from that same Go source and bakes
 * in the rest of that file's defaults internally (fragment strategy, TTL
 * trick, fake-SNI method, etc. are not exposed by the native API, only
 * listen port / remote endpoint / fake SNI / method are).
 */
object RstaSpoofConfig {

    const val LISTEN_HOST = AppConfig.LOOPBACK
    const val LISTEN_PORT = AppConfig.RSTA_SPOOF_LISTEN_PORT

    const val DEFAULT_CONNECT_IP = "104.18.38.202"
    const val DEFAULT_CONNECT_PORT = "443"
    const val DEFAULT_FAKE_SNI = "cdnjs.cloudflare.com"
    const val DEFAULT_METHOD = "combined"

    /** Bypass methods supported by the native engine's `method` parameter. */
    val METHODS = listOf(
        "combined",
        "fragment",
        "random_split",
        "seg2delay",
        "sni_triplicate",
        "oob",
        "fake_sni",
        "auto_ttl",
    )

    fun connectIp(): String =
        MmkvManager.decodeSettingsString(AppConfig.PREF_RSTA_SPOOF_CONNECT_IP, DEFAULT_CONNECT_IP)
            ?.trim().orEmpty().ifBlank { DEFAULT_CONNECT_IP }

    fun connectPort(): Int =
        MmkvManager.decodeSettingsString(AppConfig.PREF_RSTA_SPOOF_CONNECT_PORT, DEFAULT_CONNECT_PORT)
            ?.trim()?.toIntOrNull()
            ?.takeIf { it in 1..65535 }
            ?: DEFAULT_CONNECT_PORT.toInt()

    fun fakeSni(): String =
        MmkvManager.decodeSettingsString(AppConfig.PREF_RSTA_SPOOF_FAKE_SNI, DEFAULT_FAKE_SNI)
            ?.trim().orEmpty().ifBlank { DEFAULT_FAKE_SNI }

    fun method(): String =
        MmkvManager.decodeSettingsString(AppConfig.PREF_RSTA_SPOOF_METHOD, DEFAULT_METHOD)
            ?.trim().orEmpty().ifBlank { DEFAULT_METHOD }

    /**
     * True if a server profile with this address/port relies on the local
     * RSTA Spoof proxy (i.e. its outbound points at 127.0.0.1:40443).
     */
    fun isSpoofTarget(server: String?, serverPort: String?): Boolean {
        val s = server?.trim() ?: return false
        if (s != LISTEN_HOST && s.lowercase() != "localhost") return false
        val port = serverPort?.trim()?.toIntOrNull() ?: return false
        return port == LISTEN_PORT
    }
}
