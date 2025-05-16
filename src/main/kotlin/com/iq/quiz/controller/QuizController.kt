package com.iq.quiz.controller

import com.iq.quiz.Dto.QuizDTO
import com.iq.quiz.Dto.QuizWithQuestionsDto
import com.iq.quiz.Entity.Quiz
import com.iq.quiz.Entity.QuizStatus
import com.iq.quiz.Repository.QuizRepository
import com.iq.quiz.service.QuizScheduleService
import com.iq.quiz.service.QuizService
import com.iq.quiz.service.quizSpecification
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api/quiz")
class QuizController(
    private val quizService: QuizService,
    private val quizRepository: QuizRepository
) {

    private val logger: Logger = LoggerFactory.getLogger(QuizController::class.java)

    @PostMapping
    fun createQuiz(
        @RequestParam quizName: String,
        @RequestParam timer: Long,
        @RequestParam file: MultipartFile
    ): ResponseEntity<QuizWithQuestionsDto> {
        val response = quizService.createNewQuiz(quizName, timer, file)
        logger.info("Created quiz: $response")
        return ResponseEntity.status(201).body(response)
    }

    @DeleteMapping("/{quizId}")
    fun deleteQuiz(@PathVariable quizId: String): ResponseEntity<Void> {
        quizService.deleteQuiz(quizId)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/{id}")
    fun editQuiz(
        @PathVariable id: String,
        @RequestParam quizName: String?,
        @RequestParam timer: Long?,
        @RequestParam file: MultipartFile?
    ): ResponseEntity<QuizWithQuestionsDto> {
        val response = quizService.editQuiz(id, quizName, timer, file)
        logger.info("Quiz edited with id $id: $response")
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{quizId}")
    fun getQuizDto(
        @PathVariable("quizId") quizId: String
    ) :ResponseEntity<QuizWithQuestionsDto>{
        val response = quizService.getQuizWithQuestions(quizId)
        return ResponseEntity.status(HttpStatus.OK).body(response)
    }


    @GetMapping
    fun getQuizzes(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) startDate: String?,
        @RequestParam(required = false) endDate: String?,
        @RequestParam(required = false) status: QuizStatus?
    ): ResponseEntity<List<Quiz>> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val startDateTime = startDate?.let { LocalDate.parse(it, formatter).atStartOfDay() }
        val endDateTime = endDate?.let { LocalDate.parse(it, formatter).atTime(LocalTime.MAX) }

        val spec = quizSpecification(search, startDateTime, endDateTime, status)
        val quizzes = quizRepository.findAll(spec)
        return ResponseEntity.ok(quizzes)
    }

    @GetMapping("/search")
    fun searchQuizzes(@RequestParam keyword: String): ResponseEntity<List<QuizDTO>> {
        val results = quizService.searchQuizzes(keyword)
        return ResponseEntity.ok(results)
    }

}

