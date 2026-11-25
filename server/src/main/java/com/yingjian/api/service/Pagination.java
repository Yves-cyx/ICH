package com.yingjian.api.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

record Pagination(Integer size, long offset) {
    static Pagination of(String page, String size) {
        // 两个参数都省略时返回完整列表，兼容图鉴的全量缓存。
        if (page == null && size == null) return new Pagination(null, 0);
        int pageNumber = parse(page, 1);
        int pageSize = parse(size, 20);
        if (pageNumber < 1 || pageSize < 1 || pageSize > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "页码须大于 0，页大小须为 1 至 50");
        }
        return new Pagination(pageSize, (long) (pageNumber - 1) * pageSize);
    }

    private static int parse(String value, int defaultValue) {
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException error) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "分页参数必须是整数");
        }
    }

    boolean enabled() { return size != null; }
}
