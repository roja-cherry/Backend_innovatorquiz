package com.iq.quiz.service

import com.iq.quiz.Dto.QuizAttemptDTO
import com.iq.quiz.Dto.ScheduleDto
import com.iq.quiz.Dto.schedule.ScheduleWithQuestionsDto
import com.iq.quiz.Entity.AnswerSubmission
import com.iq.quiz.Entity.QuizAttempt
import com.iq.quiz.Entity.Schedule
import com.iq.quiz.Repository.*
import com.iq.quiz.exception.AlreadyAttemptedException
import com.iq.quiz.Repository.UserRepository
import com.iq.quiz.exception.ScheduleException
import com.iq.quiz.mapper.questionToDto
import com.iq.quiz.mapper.questionToQuestionWithoutAnswerDto
import com.iq.quiz.mapper.scheduleToDto
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ParticipantService(
    private val attemptRepo: QuizAttemptRepository,
    private val submissionRepo: AnswerSubmissionRepository,
    private val questionRepo: QuestionRepository,
    private val userRepo: UserRepository,
    private val scheduleService: QuizScheduleService,
    private val scheduleRepo: ScheduleRepository
) {

    /**
     * @param userId     the participant’s ID
     * @param scheduleId ID of the scheduled quiz
     * @param answers    map of questionId → selectedAnswerText
     * @return the QuizAttempt with score & maxScore populated
     */
    @Transactional
    fun submitAndScore(
        userId: String,
        scheduleId: String,
        answers: Map<String, String>
    ): QuizAttempt {
        // 1) load user & schedule
        val user = userRepo.findById(userId)
            .orElseThrow { RuntimeException("User $userId not found") }
        val schedule = scheduleRepo.findById(scheduleId)
            .orElseThrow { RuntimeException("Schedule $scheduleId not found") }

        // ① Check for an existing, already‑finished attempt
        val existing = attemptRepo.findByUserUserIdAndScheduleId(userId, scheduleId)
        if (existing != null && existing.finishedAt != null) {
            throw AlreadyAttemptedException("User $userId has already submitted this quiz.")
        }

        // ② If none (or in‑progress only), go on to record answers…
        val attempt = existing
            ?: attemptRepo.save(QuizAttempt(user = user, schedule = schedule))

        // 3) record each answer
        answers.forEach { (qId, selectedText) ->
            val question = questionRepo.findById(qId)
                .orElseThrow { RuntimeException("Question $qId not found") }

            val isCorrect = selectedText == question.correctAnswer
            submissionRepo.save(
                AnswerSubmission(
                    attempt        = attempt,
                    question       = question,
                    selectedAnswer = selectedText,
                    correct        = isCorrect
                )
            )
        }

        // 4) compute final score
        val subs          = submissionRepo.findAllByAttemptId(attempt.id!!)
        val totalQuestions = subs.size
        val correctCount   = subs.count { it.correct }

        // 5) update and persist attempt
        attempt.apply {
            finishedAt = LocalDateTime.now()
            score      = correctCount
            maxScore   = totalQuestions
        }
        return attemptRepo.save(attempt)
    }

    fun getScheduleWithQuestion(scheduleId: String): ScheduleWithQuestionsDto {
        val schedule = scheduleService.getScheduleById(scheduleId)
        val questions = questionRepo.findByQuizQuizId(schedule.quiz.quizId!!)

        return ScheduleWithQuestionsDto(
            schedule = scheduleToDto(schedule),
            questions = questions.map { questionToQuestionWithoutAnswerDto(it) },
            timer = schedule.quiz.timer
        )
    }

    fun getAttemptById(id: String): QuizAttemptDTO {
        val attempt = attemptRepo.findById(id)
            .orElseThrow { NoSuchElementException("QuizAttempt with id $id not found") }

        return QuizAttemptDTO(
            id = attempt.id!!,
            userId = attempt.user.userId!!,
            userName = attempt.user.username,
            scheduleId = attempt.schedule.id!!,
            startedAt = attempt.startedAt,
            finishedAt = attempt.finishedAt,
            score = attempt.score,
            maxScore = attempt.maxScore
        )
    }

    fun getAttemptByScheduleAndUser(userId: String,scheduleId: String): QuizAttemptDTO {
        val attempt = attemptRepo.findByUserUserIdAndScheduleId(userId,scheduleId)
            ?: throw NoSuchElementException("No QuizAttempt found for userId=$userId and scheduleId=$scheduleId")

        return QuizAttemptDTO(
            id = attempt.id!!,
            userId = attempt.user.userId!!,
            userName = attempt.user.username,
            scheduleId = attempt.schedule.id!!,
            startedAt = attempt.startedAt,
            finishedAt = attempt.finishedAt,
            score = attempt.score,
            maxScore = attempt.maxScore
        )
    }



}
