package com.yingjian.api.service;

import com.yingjian.api.mapper.FavoriteMapper;
import com.yingjian.api.model.Models.HeritagePage;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FavoriteService {
    private final FavoriteMapper favorites;
    private final CatalogService catalog;

    public FavoriteService(FavoriteMapper favorites, CatalogService catalog) {
        this.favorites = favorites;
        this.catalog = catalog;
    }

    public HeritagePage list(long memberId, String page, String size) {
        Pagination pagination = Pagination.of(page, size);
        var items = favorites.list(memberId, pagination.size(), pagination.offset());
        Long total = pagination.enabled() ? favorites.count(memberId) : null;
        return new HeritagePage(items, total);
    }

    public void add(long memberId, long heritageId) {
        checkHeritageId(heritageId);
        catalog.detail(heritageId);
        favorites.add(memberId, heritageId);
    }

    public void remove(long memberId, long heritageId) {
        checkHeritageId(heritageId);
        favorites.remove(memberId, heritageId);
    }

    private void checkHeritageId(long heritageId) {
        if (heritageId < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "非遗条目编号必须大于 0");
        }
    }
}
