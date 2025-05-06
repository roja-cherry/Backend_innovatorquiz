package com.iq.quiz.controller

import com.iq.quiz.Dto.QuizDTO
import com.iq.quiz.Dto.QuizWithQuestionsDto
import com.iq.quiz.Entity.QuizStatus
import com.iq.quiz.service.AdminService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/admin/quiz")
class AdminController(
    private val adminService: AdminService
) {

    @PostMapping("/create")
    fun createQuiz(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("quizName") quizName: String,
        @RequestParam("duration") duration: Int
    ): ResponseEntity<QuizWithQuestionsDto> {
        val response = adminService.processQuizFile(file, quizName, duration)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PutMapping("/{id}")
    fun editQuiz(
        @RequestParam("file") file: MultipartFile?,
        @RequestParam("quizName") quizName: String?,
        @RequestParam("duration") duration: Int?,
        @PathVariable id: String
    ) : ResponseEntity<QuizWithQuestionsDto> {
        val response = adminService.editQuiz(file, quizName, duration, id)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{quizId}")
    fun getQuizDto(
        @PathVariable("quizId") quizId: String
    ) :ResponseEntity<QuizDTO>{
        val response = adminService.getQuizDto(quizId)
        return ResponseEntity.status(HttpStatus.OK).body(response)
    }

    @GetMapping("/quizzes/search")
    fun searchQuizzes(@RequestParam keyword: String): ResponseEntity<List<QuizDTO>> {
        val results = adminService.searchQuizzes(keyword)
        return ResponseEntity.ok(results)
    }

    @DeleteMapping("/quizzes/{id}")
    fun deleteQuiz(@PathVariable id: String): ResponseEntity<String> {
        adminService.deleteQuizById(id)  // Calls the service method to delete the quiz
        return ResponseEntity.ok("Quiz deleted successfully")  // Returns a success message
    }

    @GetMapping("/quizzes")
    fun getAllQuizzesForAdmin(
        @RequestParam(required = false) search: String?,                     // search by name
        @RequestParam(required = false) minDuration: Int?,                  // filter by min duration
        @RequestParam(required = false) status: QuizStatus?,                // filter by status
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        createdAfter: LocalDateTime?                                        // filter by createdAt
    ): List<QuizDTO> {
        return adminService.getAllQuizzesForAdmin(search, minDuration, status, createdAfter)
    }



}