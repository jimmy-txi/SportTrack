package fr.utc.miage.sporttrack.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class baseController {


    @GetMapping("/hello")
    public String hello() {
        return "Hello, World!";
    }
    // super simple endpoint to test if the server is running
    //

    @GetMapping("/")
    public String home() {
        return "Welcome to the SportTrack !";
    }
}