package com.wanim_ms.wanimlibrary.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.*
import java.util.function.Function
import javax.crypto.SecretKey

/**
 * JWT token service for creating and validating JWT tokens.
 * 
 * Usage:
 * ```kotlin
 * @Bean
 * fun jwtService() = JwtService(secretKey = "your-256-bit-secret-key")
 * ```
 */
class JwtService(
    private var secretKey: String,
    private var tokenValidity: Long = DEFAULT_TOKEN_VALIDITY,
    private var issuer: String = DEFAULT_ISSUER
) {
    companion object {
        const val DEFAULT_TOKEN_VALIDITY = 1000L * 60 * 60 * 24 // 24 hours
        const val DEFAULT_ISSUER = "wanim-library"
    }

    private fun getSignKey(): SecretKey = Keys.hmacShaKeyFor(secretKey.toByteArray())

    /**
     * Extract a specific claim from the token.
     */
    fun <T> extractClaim(token: String, claimsResolver: Function<Claims, T>): T {
        val claims = extractAllClaims(token)
        return claimsResolver.apply(claims)
    }

    /**
     * Extract all claims from the token.
     */
    fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(getSignKey())
            .build()
            .parseSignedClaims(token)
            .payload
    }

    /**
     * Extract the subject (usually user ID) from the token.
     */
    fun extractSubject(token: String): String {
        return extractClaim(token) { it.subject }
    }

    /**
     * Extract the expiration date from the token.
     */
    fun extractExpiration(token: String): Date {
        return extractClaim(token) { it.expiration }
    }

    /**
     * Check if the token is expired.
     */
    fun isTokenExpired(token: String): Boolean {
        return try {
            extractExpiration(token).before(Date())
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Validate the token format and signature.
     */
    fun isValidToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
            !isTokenExpired(token)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Generate a new JWT token.
     */
    fun generateToken(subject: String, claims: Map<String, Any> = emptyMap()): String {
        return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuer(issuer)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + tokenValidity))
            .signWith(getSignKey())
            .compact()
    }

    /**
     * Generate a token with custom expiration.
     */
    fun generateToken(subject: String, claims: Map<String, Any>, validityMs: Long): String {
        return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuer(issuer)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + validityMs))
            .signWith(getSignKey())
            .compact()
    }

    /**
     * Refresh a token (generate new token with same claims).
     */
    fun refreshToken(token: String): String {
        val claims = extractAllClaims(token)
        return generateToken(claims.subject, claims.toMutableMap())
    }
}
