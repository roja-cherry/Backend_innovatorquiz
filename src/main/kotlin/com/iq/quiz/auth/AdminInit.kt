package com.iq.quiz.auth

import com.iq.quiz.Entity.User
import com.iq.quiz.Entity.UserRole
import com.iq.quiz.Repository.UserRepository
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AdminInit {

    @Bean
    fun createAdmin(userRepository: UserRepository) = ApplicationRunner {
        val adminMail = "admin@ibsplc.com"
        val adminName = "admin"
        val adminPassword = "admin123"

        val exists = userRepository.findByEmailAndUsername(adminMail, adminName)
        if(exists.isEmpty) {
            userRepository.save(
                User(username = adminName, email = adminMail, role = UserRole.ADMIN, password = adminPassword)
            )

        }
    }
}