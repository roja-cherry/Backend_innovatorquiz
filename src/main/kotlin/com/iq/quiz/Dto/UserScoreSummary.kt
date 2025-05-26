package com.iq.quiz.Dto

interface UserScoreSummary {
    val userId: String
    val userName: String
    val totalScore: Int?
}