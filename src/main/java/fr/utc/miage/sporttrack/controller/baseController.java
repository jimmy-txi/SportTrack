package fr.utc.miage.sporttrack.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/")
public class baseController {


    @GetMapping("/hello")
    @ResponseBody
    public String hello() {
        return "Hello, World!";
    }
    // super simple endpoint to test if the server is running
    //

    @GetMapping("/")
    public String home() {
        return "index";
    }
}