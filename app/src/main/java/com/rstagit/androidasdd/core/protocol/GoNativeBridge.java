package com.rstagit.androidasdd.core.protocol;

/**
 * Low-level JNI entry points into the precompiled RSTA Spoof native engine
 * (libandroidasdd_jni.so + libsnispf.so, bundled under app/libs/&lt;abi&gt;/).
 *
 * IMPORTANT: this class's package name and method signatures are NOT cosmetic.
 * They are the exact JNI symbol contract that libandroidasdd_jni.so was compiled
 * against (see Java_com_rstagit_androidasdd_core_protocol_GoNativeBridge_* in
 * jni_bridge.c). Renaming the package, class, or method signatures here would
 * break native method resolution at runtime (UnsatisfiedLinkError) unless the
 * native libraries are recompiled to match. Since we don't have an Android NDK
 * toolchain to rebuild those .so files, this class intentionally keeps the
 * original vendored package name instead of being moved under com.v2ray.ang.*.
 *
 * Everywhere else in the app, use {@link com.v2ray.ang.rsta.RstaSpoofEngine}
 * instead of calling this class directly - that's the clean, app-facing API.
 *
 * Bug fix note: the project this was vendored from (androidasdd_v5.zip) shipped
 * this file with the jni_bridge.c C source accidentally pasted in as its content,
 * so the JNI bridge could never have compiled. This is the corrected Java class.
 */
public final class GoNativeBridge {

    private static volatile boolean libraryLoaded = false;
    private static Throwable loadError = null;

    static {
        try {
            System.loadLibrary("androidasdd_jni");
            libraryLoaded = true;
        } catch (Throwable t) {
            // Keep this non-fatal: devices/ABIs without the native lib simply
            // won't be able to use RSTA Spoof. Callers must check isAvailable().
            loadError = t;
            libraryLoaded = false;
        }
    }

    private GoNativeBridge() {
    }

    /** True if the native engine could be loaded on this device/ABI. */
    public static boolean isAvailable() {
        return libraryLoaded;
    }

    public static Throwable getLoadError() {
        return loadError;
    }

    /**
     * Starts the local SNI-spoof proxy.
     *
     * @param listenPort     local TCP port to listen on (RSTA NG always uses 40443)
     * @param remoteEndpoint real upstream destination, formatted "host:port"
     * @param fakeSni        fake SNI/domain fronting hostname to present to DPI
     * @param method         bypass method name (e.g. "combined", "fragment", "fake_sni", "auto_ttl")
     * @return an opaque session id (non-zero on success, 0 on failure)
     */
    public static native long spfStart(int listenPort, String remoteEndpoint, String fakeSni, String method);

    /** Stops a previously started session. Safe to call with a stale/zero id. */
    public static native void spfStop(long sessionId);

    /**
     * Polls one buffered log line from the native engine, or null if there is
     * none pending right now. Intended to be called periodically from a
     * background coroutine while a session is active.
     */
    public static native String spfPollLog();

    /** Parses a raw byte buffer as a TLS ClientHello and extracts the SNI, if any. */
    public static native String spfParseSni(byte[] data, int length);

    /** Returns the native engine's version string. */
    public static native String spfVersion();
}
