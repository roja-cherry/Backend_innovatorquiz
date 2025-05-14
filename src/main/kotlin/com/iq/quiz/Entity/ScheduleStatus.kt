package com.iq.quiz.Entity

enum class ScheduleStatus(val text: String) {
    SCHEDULED("Scheduled"),
    LIVE("Live"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    fun getStatusText(): String {
        return text
    }

}