package com.iq.quiz.Entity

import com.iq.quiz.Entity.Question
import com.iq.quiz.Entity.QuizAttempt
import jakarta.persistence.*

@Entity
@Table(name = "answer_submissions")
data class AnswerSubmission(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    val attempt: QuizAttempt,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    val question: Question,

    /** the exact text the user selected (e.g. "Paris") */
    val selectedAnswer: String,

    /** true if selectedAnswer == question.correctAnswer */
    val correct: Boolean
)
