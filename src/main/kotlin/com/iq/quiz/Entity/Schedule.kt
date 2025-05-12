package com.iq.quiz.Entity

import jakarta.persistence.*
import jakarta.validation.constraints.Future
import java.time.LocalDateTime

@Entity
data class Schedule(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id : String ?= null,

    @Future
    var startDateTime: LocalDateTime ?= null,

    @Future
    var endDateTime: LocalDateTime ?= null,

    val createdAt: LocalDateTime,

    val updatedAt: LocalDateTime,

    @Enumerated(EnumType.STRING)
    var status: ScheduleStatus,

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    val quiz : Quiz,
)
