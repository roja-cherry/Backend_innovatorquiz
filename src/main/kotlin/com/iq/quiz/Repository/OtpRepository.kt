package com.iq.quiz.Repository

import com.iq.quiz.Entity.Otp
import com.iq.quiz.Entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface OtpRepository: JpaRepository<Otp, Long> {
    @Query("SELECT o FROM Otp o WHERE o.user = :user ORDER BY o.createdAt DESC LIMIT 1")
    fun findLatestByUser(@Param("user") user: User): Optional<Otp>
}