package com.yingjian.api.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yingjian.api.mapper.WorkMapper;
import com.yingjian.api.model.Models.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WorkService {
    private final WorkMapper works;

    public WorkService(WorkMapper works) { this.works = works; }

    public List<WorkCard> list(long owner) {
        return works.selectList(Wrappers.<Work>query().eq("owner_id", owner).orderByDesc("id"))
                .stream().map(WorkCard::new).toList();
    }

    public WorkCard create(long owner, WorkInput input) {
        Work work = new Work();
        work.ownerId = owner;
        work.name = input.name().strip();
        work.shape = input.shape();
        work.palette = input.palette();
        work.x = input.x();
        work.y = input.y();
        work.angle = input.angle();
        works.insert(work);
        return new WorkCard(work);
    }

    @Transactional
    public WorkCard update(long owner, long id, WorkInput input) {
        // 所有写入同时检查作品 ID 和所有者。
        works.update(null, Wrappers.<Work>update().eq("id", id).eq("owner_id", owner)
                .set("name", input.name().strip()).set("shape", input.shape()).set("palette", input.palette())
                .set("x", input.x()).set("y", input.y()).set("angle", input.angle()));
        Work saved = works.selectOne(Wrappers.<Work>query().eq("id", id).eq("owner_id", owner));
        if (saved == null) throw missing();
        return new WorkCard(saved);
    }

    public void delete(long owner, long id) {
        if (works.delete(Wrappers.<Work>query().eq("id", id).eq("owner_id", owner)) == 0) throw missing();
    }

    private ResponseStatusException missing() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "该作品不存在");
    }
}
