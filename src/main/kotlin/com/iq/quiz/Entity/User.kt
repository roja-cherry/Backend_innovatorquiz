package com.iq.quiz.Entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val userId : String ?= null,

    val username : String,

    @Column(unique = true)
    val email : String,

    @JsonIgnore
    val password : String ?= null,

    @Enumerated(EnumType.STRING)
    val role : UserRole,

    var createdAt: LocalDateTime ?= null
) {
    @PrePersist
    fun addCreatedAt() {
        createdAt = LocalDateTime.now()
    }
}

