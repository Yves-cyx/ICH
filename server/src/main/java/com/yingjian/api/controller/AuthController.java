package com.yingjian.api.controller;

import com.yingjian.api.model.Models.*;
import com.yingjian.api.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final MemberService members;
    public AuthController(MemberService members) { this.members = members; }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    Envelope<Token> register(@Valid @RequestBody Credentials input) {
        return Envelope.ok(members.register(input));
    }

    @PostMapping("/login")
    Envelope<Token> login(@Valid @RequestBody Credentials input) {
        return Envelope.ok(members.login(input));
    }
}
