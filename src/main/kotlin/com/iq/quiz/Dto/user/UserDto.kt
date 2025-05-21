package com.iq.quiz.Dto.user

import com.iq.quiz.Entity.UserRole

data class UserDto(
    val userId: String,
    val email: String,
    val username: String,
    val role: UserRole
) {
    fun getRoleText(): String {
        return role.text
    }
}
