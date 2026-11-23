package com.yingjian.api.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yingjian.api.mapper.HeritageMapper;
import com.yingjian.api.model.Models.Heritage;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CatalogService {
    private final HeritageMapper heritage;

    public CatalogService(HeritageMapper heritage) { this.heritage = heritage; }

    public List<Heritage> list() {
        return heritage.selectList(Wrappers.<Heritage>query().orderByAsc("id"));
    }

    public Heritage detail(long id) {
        Heritage entry = heritage.selectById(id);
        if (entry == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "该非遗项目不存在");
        return entry;
    }
}
