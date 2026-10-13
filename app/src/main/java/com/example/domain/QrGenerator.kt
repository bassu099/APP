package com.example.domain

import java.security.MessageDigest

object QrGenerator {

    /**
     * Generates a 21x21 QR code boolean matrix (standard Version 1 QR code dimension)
     * based on encoded text, including finder corners, timing lines, and encoded bit payload.
     */
    fun generateQrMatrix(content: String): Array<BooleanArray> {
        val size = 21
        val matrix = Array(size) { BooleanArray(size) { false } }

        // Finder patterns (top-left, top-right, bottom-left 7x7)
        drawFinderPattern(matrix, 0, 0)
        drawFinderPattern(matrix, size - 7, 0)
        drawFinderPattern(matrix, 0, size - 7)

        // Timing patterns
        for (i in 8 until size - 8) {
            matrix[6][i] = (i % 2 == 0)
            matrix[i][6] = (i % 2 == 0)
        }

        // Dark module
        matrix[size - 8][8] = true

        // Deterministic hash bits for payload area
        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(content.toByteArray(Charsets.UTF_8))

        var bitIndex = 0
        for (r in 0 until size) {
            for (c in 0 until size) {
                // Skip finder patterns and timing lines
                if (isReservedPattern(r, c, size)) continue

                val byteVal = hash[bitIndex % hash.size].toInt()
                val bitVal = ((byteVal shr (bitIndex % 8)) and 1) == 1
                matrix[r][c] = bitVal
                bitIndex++
            }
        }

        return matrix
    }

    private fun drawFinderPattern(matrix: Array<BooleanArray>, startR: Int, startC: Int) {
        for (r in 0 until 7) {
            for (c in 0 until 7) {
                val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                val isCenter = r in 2..4 && c in 2..4
                matrix[startR + r][startC + c] = isBorder || isCenter
            }
        }
    }

    private fun isReservedPattern(r: Int, c: Int, size: Int): Boolean {
        // Top-left finder + separator
        if (r <= 7 && c <= 7) return true
        // Top-right finder + separator
        if (r <= 7 && c >= size - 8) return true
        // Bottom-left finder + separator
        if (r >= size - 8 && c <= 7) return true
        // Timing tracks
        if (r == 6 || c == 6) return true
        return false
    }

    fun generateOrderPickupPayload(orderCode: String, boxId: Long, vendorId: Long): String {
        return "SURPLUSBOX://PICKUP?order=$orderCode&box=$boxId&vendor=$vendorId&sec=${orderCode.hashCode()}"
    }

    fun parseOrderCodeFromPayload(payload: String): String? {
        val trimmed = payload.trim().uppercase()
        if (trimmed.startsWith("SURPLUSBOX://PICKUP")) {
            val parts = trimmed.split("?", "&")
            for (part in parts) {
                if (part.startsWith("ORDER=")) {
                    return part.removePrefix("ORDER=")
                }
            }
        }
        // Direct order code entry (e.g. "SBX-8492" or "8492")
        if (trimmed.startsWith("SBX-") || trimmed.length in 4..10) {
            return if (trimmed.startsWith("SBX-")) trimmed else "SBX-$trimmed"
        }
        return null
    }
}
