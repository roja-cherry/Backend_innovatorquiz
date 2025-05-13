package com.iq.quiz.service

import com.iq.quiz.Dto.QuizDTO
import com.iq.quiz.Entity.Quiz
import com.iq.quiz.Entity.QuizStatus
import com.iq.quiz.Repository.QuestionRepository
import com.iq.quiz.Repository.QuizRepository
import com.iq.quiz.exception.QuizNotFoundException
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import java.time.LocalDateTime

class AdminServiceTest {

    private lateinit var quizRepository: QuizRepository
    private lateinit var questionRepository: QuestionRepository
    private lateinit var adminService: AdminService

    @BeforeEach
    fun setUp() {
        quizRepository = mockk(relaxed = true) // Using relaxed mock to avoid unnecessary stubbing
        questionRepository = mockk(relaxed = true)
        adminService = AdminService(quizRepository, questionRepository)
    }

    @Test
    fun `getQuizDto should return quiz DTO if quiz exists`() {
        val quiz = Quiz(
            quizId = "123",
            quizName = "Sample Quiz",
            timer = 30,
            status = QuizStatus.CREATED,
            createdBy = null,
            createdAt = LocalDateTime.now(),
            isActive = true
        )

        every { quizRepository.findByQuizId("123") } returns quiz

        val result = adminService.getQuizDto("123")

        assertNotNull(result)
        assertEquals("Sample Quiz", result?.quizName)
    }

    @Test
    fun `getQuizDto should throw exception if quiz not found`() {
        every { quizRepository.findByQuizId("not_exist") } returns null

        val exception = assertThrows(QuizNotFoundException::class.java) {
            adminService.getQuizDto("not_exist")
        }

        assertEquals("Quiz Not Found", exception.message)
    }

    @Test
    fun `getAllQuizzesForAdmin should filter quizzes by isActive and status`() {
        val now = LocalDateTime.now()
        val quiz1 = Quiz("id1", "Quiz1", 10, QuizStatus.CREATED, null, now.minusMonths(2), true)
        val quiz2 = Quiz("id2", "Quiz2", 20, QuizStatus.COMPLETED, null, now.minusMonths(4), true)
        val quiz3 = Quiz("id3", "Quiz3", 15, QuizStatus.CREATED, null, now.minusMonths(7), true)

        every { quizRepository.findAll() } returns listOf(quiz1, quiz2, quiz3)

        val result = adminService.getAllQuizzesForAdmin(true, QuizStatus.CREATED, "3m")

        assertEquals(1, result.size)
        assertEquals("id1", result[0].quizId)
    }

    @Test
    fun `getAllQuizzesForAdmin should return quizzes created before 6 months`() {
        val now = LocalDateTime.now()
        val quizOld = Quiz("id-old", "Old Quiz", 10, QuizStatus.CREATED, null, now.minusMonths(7), true)

        every { quizRepository.findAll() } returns listOf(quizOld)

        val result = adminService.getAllQuizzesForAdmin(true, QuizStatus.CREATED, "before6m")

        assertEquals(1, result.size)
        assertEquals("id-old", result[0].quizId)
    }

    @Test
    fun `deleteQuizById should delete quiz and its questions`() {
        val quizId = "quiz-to-delete"
        val quiz = Quiz(quizId, "Delete Me", 10, QuizStatus.CREATED, null, LocalDateTime.now(), true)

        every { quizRepository.findByQuizId(quizId) } returns quiz
        every { questionRepository.deleteAllByQuizQuizId(quizId) } just Runs
        every { quizRepository.delete(quiz) } just Runs

        adminService.deleteQuizById(quizId)

        verify { questionRepository.deleteAllByQuizQuizId(quizId) }
        verify { quizRepository.delete(quiz) }
    }

    @Test
    fun `deleteQuizById should throw exception if quiz not found`() {
        every { quizRepository.findByQuizId("invalid") } returns null

        val exception = assertThrows(QuizNotFoundException::class.java) {
            adminService.deleteQuizById("invalid")
        }

        assertEquals("Quiz not found with ID: invalid", exception.message)
    }
}
