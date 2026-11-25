package com.yingjian.api.controller;

import com.yingjian.api.model.Models.*;
import com.yingjian.api.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/heritage")
public class CatalogController {
    private final CatalogService catalog;
    public CatalogController(CatalogService catalog) { this.catalog = catalog; }

    @GetMapping
    @Operation(operationId = "listHeritage", summary = "查询非遗目录，可按关键词、类别筛选和分页")
    @ApiResponse(responseCode = "200", description = "请求成功", useReturnTypeSchema = true,
            headers = @Header(name = "X-Total-Count", description = "启用分页时返回筛选后的总条数",
                    schema = @Schema(type = "integer", format = "int64")))
    Envelope<List<Heritage>> list(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) String category,
                                  @Parameter(schema = @Schema(type = "integer", minimum = "1"))
                                  @RequestParam(required = false) String page,
                                  @Parameter(schema = @Schema(type = "integer", minimum = "1", maximum = "50"))
                                  @RequestParam(required = false) String size,
                                  HttpServletResponse response) {
        HeritagePage result = catalog.search(keyword, category, page, size);
        if (result.totalCount() != null) {
            response.setHeader("X-Total-Count", result.totalCount().toString());
        }
        return Envelope.ok(result.items());
    }

    @GetMapping("/{id}")
    Envelope<Heritage> detail(@PathVariable long id) { return Envelope.ok(catalog.detail(id)); }
}
