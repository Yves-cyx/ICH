package com.yingjian.api.controller;

import com.yingjian.api.model.Models.*;
import com.yingjian.api.service.MemberService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {
    private final MemberService members;
    public ProfileController(MemberService members) { this.members = members; }

    @GetMapping
    Envelope<Profile> get(@AuthenticationPrincipal Jwt jwt) {
        return Envelope.ok(members.profile(Long.parseLong(jwt.getSubject())));
    }

    @PutMapping
    Envelope<Profile> update(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ProfileInput input) {
        return Envelope.ok(members.update(Long.parseLong(jwt.getSubject()), input));
    }
}
