//package com.example.datnsd26.controller.error;
//
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.boot.web.servlet.error.ErrorController;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.RequestMapping;
//
//@Controller
//public class CustomErrorController implements ErrorController {
//    @RequestMapping("/error")
//    public String handleError(Model model) {
//        model.addAttribute("message", "🔴 URL không hợp lệ. Vui lòng thử lại!");
//        return "error"; // Đúng tên file error.html
//    }
//}
