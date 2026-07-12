package com.example.nhviewer.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

/**
 * PoW 工作量碰撞解算器
 *
 * API 规格：difficulty 表示 SHA-256 哈希值的二进制前导零位数（Leading zero bits）。
 * 解算逻辑：对 "challenge + nonce" 做 SHA-256，检查哈希字节开头是否有 difficulty 位连续的零。
 */
object PowSolver {
    suspend fun solve(challenge: String, difficulty: Int): String = withContext(Dispatchers.Default) {
        val digest = MessageDigest.getInstance("SHA-256")
        val fullBytes = difficulty / 8
        val remainingBits = difficulty % 8

        var nonce = 0L
        while (true) {
            val candidate = "$challenge$nonce"
            val hash = digest.digest(candidate.toByteArray(Charsets.UTF_8))

            if (hasLeadingZeroBits(hash, fullBytes, remainingBits)) {
                return@withContext nonce.toString()
            }
            nonce++
        }
        @Suppress("UNREACHABLE_CODE")
        ""
    }

    /**
     * 检查 hash 的前 difficulty 位是否全为零
     * fullBytes：完整的全零字节数
     * remainingBits：剩余需要检查的位数
     */
    private fun hasLeadingZeroBits(hash: ByteArray, fullBytes: Int, remainingBits: Int): Boolean {
        for (i in 0 until fullBytes) {
            if (hash[i] != 0.toByte()) return false
        }
        if (remainingBits > 0) {
            val mask = (0xFF shl (8 - remainingBits)) and 0xFF
            if (hash[fullBytes].toInt() and mask != 0) return false
        }
        return true
    }
}
