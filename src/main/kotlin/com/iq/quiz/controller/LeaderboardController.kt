package com.iq.quiz.controller

import com.iq.quiz.Dto.UserScoreSummary
import com.iq.quiz.Entity.QuizAttempt
import com.iq.quiz.service.ParticipantService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/leaderboard")
class LeaderboardController(
    private val participantService: ParticipantService
) {

    @GetMapping("/schedule/{scheduleId}")
    fun getTop10BySchedule(@PathVariable scheduleId: String): List<UserScoreSummary> {
        return participantService.getTop10BySchedule(scheduleId)
    }

    @GetMapping("/global")
    fun getTop10GlobalLeaderboard(): List<UserScoreSummary> {
        return participantService.getGlobalLeaderboard()
    }
}
