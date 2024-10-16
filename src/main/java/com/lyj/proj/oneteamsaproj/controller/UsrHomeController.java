package com.lyj.proj.oneteamsaproj.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UsrHomeController {

//    @ResponseBody
    @RequestMapping("/usr/home/main")
    public String showMain() {
        return "main";
    }


//    @ResponseBody
    @RequestMapping("/")
    public String showRoot() {
        return "redirect:/usr/home/main";
    }

}
