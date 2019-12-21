package net.csust.webo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WeboApplication

fun main(args: Array<String>) {
    runApplication<WeboApplication>(*args)
}
