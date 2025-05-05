package com.iq.quiz.controller

import com.iq.quiz.Dto.QuizDTO
import com.iq.quiz.Dto.QuizWithQuestionsDto
import com.iq.quiz.service.AdminService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

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

    @GetMapping("/{quizid}")
    fun getQuizDto(
        @PathVariable("quizid") quizId: String
    ) :ResponseEntity<QuizDTO>{
        val response = adminService.getQuizDtoService(quizId)
        return ResponseEntity.status(HttpStatus.OK).body(response)
    }

}