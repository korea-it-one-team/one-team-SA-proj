package com.lyj.proj.oneteamsaproj.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UsrHomeController {

//    @ResponseBody
    @RequestMapping("/usr/home/main")
    public String showMain() {
        System.out.println("Authentication Info: " + SecurityContextHolder.getContext().getAuthentication());
        return "usr/home/main";
    }


//    @ResponseBody
    @RequestMapping("/")
    public String showRoot() {
        System.out.println("Authentication Info: " + SecurityContextHolder.getContext().getAuthentication());
        return "redirect:/usr/home/main";
    }

}
