package schemact.examples.springboot.demo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SampleController {
    @GetMapping("/blog")
    fun blog(): String {
        return "blog blog"
    }
}