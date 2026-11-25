package com.yingjian.api.service;

import com.yingjian.api.mapper.HeritageMapper;
import com.yingjian.api.model.Models.Heritage;
import com.yingjian.api.model.Models.HeritagePage;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CatalogService {
    private static final Set<String> CATEGORIES = Set.of("表演", "工艺", "音乐");
    private final HeritageMapper heritage;

    public CatalogService(HeritageMapper heritage) { this.heritage = heritage; }

    public HeritagePage search(String keyword, String category, String page, String size) {
        String term = keyword == null ? "" : keyword.strip();
        if (term.length() > 80) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "关键词最多 80 个字符");
        }
        if (category != null && !CATEGORIES.contains(category)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请选择有效的非遗类别");
        }
        Pagination pagination = Pagination.of(page, size);
        // 用户输入的 LIKE 通配符按普通字符处理，外层百分号用于包含匹配。
        String pattern = term.isEmpty() ? null
                : "%" + term.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
        var items = heritage.search(pattern, category, pagination.size(), pagination.offset());
        Long total = pagination.enabled() ? heritage.countMatches(pattern, category) : null;
        return new HeritagePage(items, total);
    }

    public Heritage detail(long id) {
        Heritage entry = heritage.selectById(id);
        if (entry == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "该非遗项目不存在");
        return entry;
    }
}
