package com.iq.quiz.Repository

import com.iq.quiz.Entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<User, String> {

}