package com.yingjian.api.controller;

import com.yingjian.api.model.Models.*;
import com.yingjian.api.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me/favorites")
@SecurityRequirement(name = "bearerAuth")
public class FavoriteController {
    private final FavoriteService favorites;

    public FavoriteController(FavoriteService favorites) { this.favorites = favorites; }

    @GetMapping
    @Operation(operationId = "listMyFavorites", summary = "查询当前账号的收藏")
    @ApiResponse(responseCode = "200", description = "请求成功", useReturnTypeSchema = true,
            headers = @Header(name = "X-Total-Count", description = "启用分页时返回收藏总数",
                    schema = @Schema(type = "integer", format = "int64")))
    Envelope<List<Heritage>> list(@AuthenticationPrincipal Jwt jwt,
                                  @Parameter(schema = @Schema(type = "integer", minimum = "1"))
                                  @RequestParam(required = false) String page,
                                  @Parameter(schema = @Schema(type = "integer", minimum = "1", maximum = "50"))
                                  @RequestParam(required = false) String size,
                                  HttpServletResponse response) {
        HeritagePage result = favorites.list(memberId(jwt), page, size);
        if (result.totalCount() != null) {
            response.setHeader("X-Total-Count", result.totalCount().toString());
        }
        return Envelope.ok(result.items());
    }

    @PutMapping("/{heritageId}")
    @Operation(operationId = "addMyFavorite", summary = "收藏非遗条目")
    Envelope<Void> add(@AuthenticationPrincipal Jwt jwt, @PathVariable long heritageId) {
        favorites.add(memberId(jwt), heritageId);
        return Envelope.ok(null);
    }

    @DeleteMapping("/{heritageId}")
    @Operation(operationId = "removeMyFavorite", summary = "取消收藏")
    Envelope<Void> remove(@AuthenticationPrincipal Jwt jwt, @PathVariable long heritageId) {
        favorites.remove(memberId(jwt), heritageId);
        return Envelope.ok(null);
    }

    private long memberId(Jwt jwt) {
        // 收藏归属只使用当前令牌中的账号 ID。
        return Long.parseLong(jwt.getSubject());
    }
}
