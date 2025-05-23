package com.iq.quiz.Entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class Otp(
    @Id
    @GeneratedValue
    val id: Long ?= null,
    val code: Int,

    @ManyToOne(cascade = [CascadeType.ALL])
    val user: User,

    var createdAt: LocalDateTime ?= null
) {
    @PrePersist
    fun addCreatedAt() {
        createdAt = LocalDateTime.now()
    }
}
