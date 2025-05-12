package com.iq.quiz.controller

import com.iq.quiz.Dto.PublishQuizRequest
import com.iq.quiz.Dto.QuizDTO
import com.iq.quiz.Dto.QuizWithQuestionsDto
import com.iq.quiz.Dto.UpdateIsActive
import com.iq.quiz.Entity.QuizStatus
import com.iq.quiz.service.AdminService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile


@RestController
@RequestMapping("/api/admin/quiz")
class AdminController(
    private val adminService: AdminService
) {

    @PostMapping("/create")
    fun createQuiz(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("quizName") quizName: String,
        @RequestParam("timer") timer: Int
    ): ResponseEntity<QuizWithQuestionsDto> {
        val response = adminService.processQuizFile(file, quizName, timer)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PutMapping("/{id}")
    fun editQuiz(
        @RequestParam("file") file: MultipartFile?,
        @RequestParam("quizName") quizName: String?,
        @RequestParam("timer") timer: Int?,
        @PathVariable id: String
    ) : ResponseEntity<QuizWithQuestionsDto> {
        val response = adminService.editQuiz(file, quizName, timer, id)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{quizId}")
    fun getQuizDto(
        @PathVariable("quizId") quizId: String
    ) :ResponseEntity<QuizDTO>{
        val response = adminService.getQuizDto(quizId)
        return ResponseEntity.status(HttpStatus.OK).body(response)
    }

    @GetMapping("/search")
    fun searchQuizzes(@RequestParam keyword: String): ResponseEntity<List<QuizDTO>> {
        val results = adminService.searchQuizzes(keyword)
        return ResponseEntity.ok(results)
    }

    @DeleteMapping("/{id}")
    fun deleteQuiz(@PathVariable id: String): ResponseEntity<String> {
        adminService.deleteQuizById(id)  // Calls the service method to delete the quiz
        return ResponseEntity.ok("Quiz deleted successfully")  // Returns a success message
    }

    @GetMapping("/quizzes")
    fun getAllQuizzesForAdmin(
        @RequestParam(required = false) isActive: Boolean,
        @RequestParam(required = false) status: QuizStatus?,
        @RequestParam(required = false) createdWithin: String? // "1m", "3m", "6m", "before6m"
    ): List<QuizDTO> {
        return adminService.getAllQuizzesForAdmin(isActive, status, createdWithin)
    }

    @PatchMapping("/{id}")
    fun updateIsActive(
        @PathVariable id:String,
        @RequestBody updateIsActive: UpdateIsActive
    ):ResponseEntity<QuizDTO>{
        val response=adminService.updateIsActive(id,updateIsActive.isActive)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/quiz-with-questions/{quizId}")
    fun getQuizWithQuestions(@PathVariable quizId: String): QuizWithQuestionsDto {
        return adminService.getQuizWithQuestions(quizId)
    }
}