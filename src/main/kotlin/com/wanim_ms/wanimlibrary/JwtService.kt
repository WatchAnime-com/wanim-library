package com.wanim_ms.wanimlibrary

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import java.util.function.Function
import javax.crypto.SecretKey

@Service
class JwtService {

    @Value("\${jwt.secret}")
    private lateinit var secretKey: String

    private var tokenValidity: Long = 1000 * 60 * 60 * 24 // 24 hours in milliseconds

    fun setSecretKey(secretKey: String) {
        this.secretKey = secretKey
    }

    private fun getSignKey(): SecretKey {
        return Keys.hmacShaKeyFor(secretKey.toByteArray())
    }

    fun <T> extractClaims(jwtToken: String?, claimsResolver: Function<Claims, T>): T {
        val claims = extractAllClaims(jwtToken)
        return claimsResolver.apply(claims)
    }

    fun extractClaims(jwtToken: String?): Claims {
        return extractAllClaims(jwtToken)
    }

    fun generateToken(map: HashMap<String, Any>, subject: String): String {
        return generateToken(claims = map, subject = subject)
    }

    fun isTokenExpired(token: String?): Boolean {
        return extractExpiration(token).before(Date())
    }



    private fun extractExpiration(token: String?): Date {
        return extractClaims(token) { claims -> claims.expiration }
    }

    fun extractSubject(jwtToken: String?): String {
        return extractClaims(jwtToken) { claims -> claims.subject }
    }

    private fun extractAllClaims(token: String?): Claims {
        return Jwts.parser()
            .setSigningKey(getSignKey())
            .build()
            .parseClaimsJws(token)
            .body
    }

    private fun generateToken(claims: Map<String, Any>, subject: String): String {
        return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuer("watchanim.com")
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + tokenValidity))
            .signWith(getSignKey())
            .compact()
    }
}