package com.iq.quiz.Dto.schedule

import com.iq.quiz.Entity.ScheduleStatus

data class HomePageSchedule(
    val scheduleId: String?,
    val quizName: String,
    val status: ScheduleStatus,
    val isAttempted: Boolean
) {
    fun getStatustext(): String {
        return status.text
    }
}