package com.yingjian.api.controller;

import com.yingjian.api.model.Models.*;
import com.yingjian.api.service.CatalogService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/heritage")
public class CatalogController {
    private final CatalogService catalog;
    public CatalogController(CatalogService catalog) { this.catalog = catalog; }

    @GetMapping
    Envelope<List<Heritage>> list() { return Envelope.ok(catalog.list()); }

    @GetMapping("/{id}")
    Envelope<Heritage> detail(@PathVariable long id) { return Envelope.ok(catalog.detail(id)); }
}
