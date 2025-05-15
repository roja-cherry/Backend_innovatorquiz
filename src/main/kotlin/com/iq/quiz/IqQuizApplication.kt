package com.iq.quiz

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class IqQuizApplication

fun main(args: Array<String>) {
	runApplication<IqQuizApplication>(*args)
}
