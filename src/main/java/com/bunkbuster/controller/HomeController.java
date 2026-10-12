package com.bunkbuster.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "redirect:/auth/student-login";
    }

    @GetMapping("/chatbot")
    public String chatbot() {
        return "chatbot/chatbot";
    }
}