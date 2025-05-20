package com.iq.quiz.controller

import com.iq.quiz.service.ParticipantService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/participant")
class ParticipantScheduleController(
    private val participantService: ParticipantService
) {
}