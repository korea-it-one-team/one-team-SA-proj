package com.lyj.proj.oneteamsaproj.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UsrHomeController {


    @GetMapping("/usr/home/main")
    public String mainPage() {
        // "main"은 /WEB-INF/views/main.jsp 파일을 의미
        return "main";
    }

//    @ResponseBody
//    @RequestMapping("/usr/home/main")
//    public String showMain() {
//        return "main";
//    }
//
//
//    //	@ResponseBody
//    @RequestMapping("/")
//    public String showRoot() {
//        return "redirect:/usr/home/main";
//    }

}
