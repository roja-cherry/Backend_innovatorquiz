package com.iq.quiz.auth


import com.iq.quiz.Entity.User
import com.iq.quiz.exception.AuthException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureException
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus

import org.springframework.stereotype.Component
import java.util.*

import javax.crypto.SecretKey

@Component
class JwtService {

    @Value("\${app.security.jwt.key}")
    private lateinit var secret: String

    @Value("\${app.security.jwt.expiration}")
    private var jwtExpiration: Int = 0

    private fun getKey(): SecretKey {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))
    }

    fun createToken(user: User): String {
        return Jwts.builder()
            .subject(user.email)
            .claim("userId", user.userId)
            .claim("role", user.role)
            .claim("username", user.username)
            .expiration(Date(System.currentTimeMillis() + jwtExpiration))
            .issuedAt(Date(System.currentTimeMillis()))
            .signWith(getKey())
            .compact()
    }

    fun extractClaims(token: String): Claims {
        return try {
            Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .payload

        } catch (e: Exception) {
           throw AuthException("Failed to authenticate user", HttpStatus.UNAUTHORIZED)
        }
    }
}

