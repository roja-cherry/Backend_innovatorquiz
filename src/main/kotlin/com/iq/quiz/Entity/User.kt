package com.iq.quiz.Entity

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val userId : String,

    val userName : String,
    val email : String,
    val password : String,

    @Enumerated(EnumType.STRING)
    val role : UserRole
)

