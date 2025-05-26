package com.iq.quiz.service

import com.iq.quiz.Dto.QuizAttemptDTO
import com.iq.quiz.Dto.UserScoreSummary
import com.iq.quiz.Dto.schedule.HomePageSchedule
import com.iq.quiz.Dto.schedule.ScheduleWithQuestionsDto
import com.iq.quiz.Entity.AnswerSubmission
import com.iq.quiz.Entity.QuizAttempt
import com.iq.quiz.Entity.ScheduleStatus
import com.iq.quiz.Repository.*
import com.iq.quiz.exception.AlreadyAttemptedException
import com.iq.quiz.Repository.UserRepository
import com.iq.quiz.exception.ScheduleException
import com.iq.quiz.mapper.questionToQuestionWithoutAnswerDto
import com.iq.quiz.mapper.quizAttemptToDto
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
    private val scheduleRepo: ScheduleRepository,
    private val quizAttemptRepository: QuizAttemptRepository

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
        //load user & schedule
        val user = userRepo.findById(userId)
            .orElseThrow { RuntimeException("User $userId not found") }
        val schedule = scheduleRepo.findById(scheduleId)
            .orElseThrow { RuntimeException("Schedule $scheduleId not found") }

        //Check for an existing, already‑finished attempt
        val existing = attemptRepo.findByUserUserIdAndScheduleId(userId, scheduleId)
        if (existing != null && existing.finishedAt != null) {
            throw AlreadyAttemptedException("User $userId has already submitted this quiz.")
        }

        //If none (or in‑progress only), go on to record answers…
        val attempt = existing
            ?: attemptRepo.save(QuizAttempt(user = user, schedule = schedule))

        //record each answer
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

        //compute final score correctly
        val subs = submissionRepo.findAllByAttemptId(attempt.id!!)
        val correctCount = subs.count { it.correct }
        val totalQuestions = questionRepo.findAllByQuizQuizId(schedule.quiz.quizId!!).size


        //update and persist attempt
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

        return quizAttemptToDto(attempt)
    }

    fun getTop10BySchedule(scheduleId: String): List<UserScoreSummary> {
        return quizAttemptRepository.findTop10BySchedule(scheduleId)
    }

    fun getGlobalLeaderboard(): List<UserScoreSummary> {
        return quizAttemptRepository.findTop10Global()
    }



    fun getAttemptByScheduleAndUser(userId: String,scheduleId: String): QuizAttemptDTO {
        val attempt = attemptRepo.findByUserUserIdAndScheduleId(userId,scheduleId)
            ?: throw ScheduleException("No QuizAttempt found for user", HttpStatus.NOT_FOUND)

        return quizAttemptToDto(attempt)
    }


    fun getUserHomePageSchedules(userId: String): List<HomePageSchedule> {
        val schedules = scheduleRepo.findByStatusIn(
            listOf(ScheduleStatus.ACTIVE, ScheduleStatus.COMPLETED)
        )

        return schedules.map { schedule ->
            val isAttempted = quizAttemptRepository.existsByUser_UserIdAndScheduleId(userId, schedule.id ?: "")
            HomePageSchedule(
                scheduleId = schedule.id ?: "",
                quizName = schedule.quiz.quizName,
                status = schedule.status,
                isAttempted = isAttempted
            )
        }
    }



}
