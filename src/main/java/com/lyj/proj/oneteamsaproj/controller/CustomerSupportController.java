package com.lyj.proj.oneteamsaproj.controller;

import com.lyj.proj.oneteamsaproj.service.CustomerSupportService;
import com.lyj.proj.oneteamsaproj.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import kotlin.reflect.jvm.internal.impl.descriptors.Visibilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class CustomerSupportController {

    @Autowired
    private CustomerSupportService customerSupportService;


    @Autowired
    private Rq rq;

    @RequestMapping("/usr/customer-support")
    public String showCustomerSupport(HttpServletRequest req, Model model) {
        Rq rq = (Rq) req.getAttribute("rq");
        int member_id = 0;
        String member_status = "";
        if(rq.getLoginedMemberId() != 0){
            member_id = rq.getLoginedMemberId();
            member_status = rq.getLoginedMember().getAuthLevel() == 7 ? "관리자" : "유저";
        };

        // DB에서 FAQ, 상담내역 등을 불러와서 모델에 추가
        List<faq_Categorys> categories = customerSupportService.getcategorys();
        List<Faq> faqs = customerSupportService.getFaqs();
        List<Consultation> consultations = customerSupportService.getHistory(member_id, member_status);

        model.addAttribute("categories",categories);
        model.addAttribute("faqs",faqs);
        model.addAttribute("consultations",consultations);
        if (member_status.equals("관리자")){
            model.addAttribute("isAdmin",true);
        }else {
            model.addAttribute("isAdmin",false);
        };
        return "usr/service/customer_support"; // HTML 파일 이름
    }

    @PostMapping("/submit-consultation")
    @ResponseBody
    public ResponseEntity<Consultation> submitConsultation(HttpServletRequest req, @RequestParam String title, @RequestParam String content) {

        Rq rq = (Rq) req.getAttribute("rq");
        // 상담 저장 로직

        Consultation consultation = customerSupportService.addConsultation(title,content,rq.getLoginedMemberId());

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