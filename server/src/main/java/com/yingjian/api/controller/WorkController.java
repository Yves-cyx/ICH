package com.yingjian.api.controller;

import com.yingjian.api.model.Models.*;
import com.yingjian.api.service.WorkService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/works")
@SecurityRequirement(name = "bearerAuth")
public class WorkController {
    private final WorkService works;
    public WorkController(WorkService works) { this.works = works; }

    @GetMapping
    Envelope<List<WorkCard>> list(@AuthenticationPrincipal Jwt jwt) {
        return Envelope.ok(works.list(Long.parseLong(jwt.getSubject())));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Envelope<WorkCard> create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody WorkInput input) {
        return Envelope.ok(works.create(Long.parseLong(jwt.getSubject()), input));
    }

    @PutMapping("/{id}")
    Envelope<WorkCard> update(@AuthenticationPrincipal Jwt jwt, @PathVariable long id, @Valid @RequestBody WorkInput input) {
        return Envelope.ok(works.update(Long.parseLong(jwt.getSubject()), id, input));
    }

    @DeleteMapping("/{id}")
    Envelope<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) {
        works.delete(Long.parseLong(jwt.getSubject()), id);
        return Envelope.ok(null);
    }
}
