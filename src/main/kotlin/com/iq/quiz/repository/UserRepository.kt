package com.iq.quiz.Repository

import com.iq.quiz.Entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, String> {
    fun findByEmailAndUsername(email: String, username: String): Optional<User>
}