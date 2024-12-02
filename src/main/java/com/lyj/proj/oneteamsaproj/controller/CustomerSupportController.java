package com.lyj.proj.oneteamsaproj.controller;

import com.lyj.proj.oneteamsaproj.service.CustomerSupportService;
import com.lyj.proj.oneteamsaproj.service.LoginService;
import com.lyj.proj.oneteamsaproj.utils.RqUtil;
import com.lyj.proj.oneteamsaproj.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class CustomerSupportController {

    @Autowired
    private CustomerSupportService customerSupportService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private RqUtil rq;

    @RequestMapping("/customer-support")
    public String showCustomerSupport(HttpServletRequest req, Model model) {

        Member member = null;
        boolean isAdmin = false;

        if (loginService.isLogined()) {
            member = loginService.getLoginedMember();
            isAdmin = member.isAdmin();
        }

        // DB에서 FAQ, 상담내역 등을 불러와서 모델에 추가
        List<faq_Categorys> categories = customerSupportService.getcategorys();
        List<Faq> faqs = customerSupportService.getFaqs();
        List<Consultation> consultations = customerSupportService.getHistory(loginService.getLoginedMemberId(), isAdmin);

        model.addAttribute("categories", categories);
        model.addAttribute("faqs", faqs);
        model.addAttribute("consultations", consultations);
        if (isAdmin){
            model.addAttribute("isAdmin",true);
        }else {
            model.addAttribute("isAdmin",false);
        }
        return "usr/service/customer_support"; // HTML 파일 이름
    }

    @PostMapping("/submit-consultation")
    @ResponseBody
    public ResponseEntity<Consultation> submitConsultation(@RequestParam String title, @RequestParam String content) {

        System.out.println("title : " + title);
        System.out.println("content : " + content);
        System.out.println("loginedMemberId : " + loginService.getLoginedMemberId());

        // 상담 저장 로직
        Consultation consultation = customerSupportService.addConsultation(title, content, loginService.getLoginedMemberId());

        // 저장된 상담 객체 반환
        return ResponseEntity.ok(consultation);
    }

    @PostMapping("/save-answer")
    @ResponseBody
    public String saveAnswer(@RequestParam int id, @RequestParam String answer) {
        customerSupportService.updateAnswer(id, answer);
        return "redirect:/consultation-history"; // 상담 내역 페이지로 리다이렉트
    }
}