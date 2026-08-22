package com.example.engine

import android.util.Log

/**
 * JNI bridge interface for llama.cpp native shared libraries (libllama.so, libggml.so, libllama-android.so).
 * Configured in build.gradle.kts with NDK ABI filters (arm64-v8a, armeabi-v7a, x86_64, x86) and JNI sourceSets.
 */
object LlamaCppBridge {
    private const val TAG = "LlamaCppBridge"
    private var isNativeLibraryLoaded = false

    init {
        try {
            System.loadLibrary("ggml")
            System.loadLibrary("llama")
            isNativeLibraryLoaded = true
            Log.i(TAG, "Successfully loaded native llama.cpp JNI libraries (libggml.so & libllama.so)")
        } catch (e1: Throwable) {
            try {
                System.loadLibrary("llama-android")
                isNativeLibraryLoaded = true
                Log.i(TAG, "Successfully loaded native llama-android JNI library")
            } catch (e2: Throwable) {
                Log.i(TAG, "Native .so binaries running in flexible on-device fallback mode: ${e2.message}")
                isNativeLibraryLoaded = false
            }
        }
    }

    /**
     * Checks whether the native compiled C++ llama engine is currently loaded in the JVM runtime.
     */
    fun isNativeEngineAvailable(): Boolean = isNativeLibraryLoaded

    fun safeInitBackend(): Boolean {
        if (!isNativeLibraryLoaded) return false
        return try {
            initNativeBackend()
        } catch (t: Throwable) {
            Log.w(TAG, "Native initBackend failed: ${t.message}")
            false
        }
    }

    // Native JNI function signatures matching llama.cpp Android interface
    private external fun initNativeBackend(): Boolean
    private external fun loadModelNative(modelPath: String, contextSize: Int, gpuLayers: Int, threads: Int): Long
    private external fun freeModelNative(contextPtr: Long)
    private external fun tokenizeNative(contextPtr: Long, text: String): IntArray
    private external fun evalNative(contextPtr: Long, tokens: IntArray, nPast: Int): FloatArray
    private external fun sampleTokenNative(contextPtr: Long, logits: FloatArray, temperature: Float, topP: Float): Int
    private external fun tokenToStrNative(contextPtr: Long, token: Int): String
    private external fun getSystemInfoNative(): String
}
