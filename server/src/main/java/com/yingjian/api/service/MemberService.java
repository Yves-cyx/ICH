package com.yingjian.api.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yingjian.api.mapper.MemberMapper;
import com.yingjian.api.model.Models.*;
import com.yingjian.api.security.TokenService;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MemberService {
    private final MemberMapper members;
    private final PasswordEncoder passwords;
    private final TokenService tokens;
    private final String unknownUserHash;

    public MemberService(MemberMapper members, PasswordEncoder passwords, TokenService tokens) {
        this.members = members;
        this.passwords = passwords;
        this.tokens = tokens;
        this.unknownUserHash = passwords.encode("unknown-user-" + java.util.UUID.randomUUID());
    }

    public Token register(Credentials input) {
        checkPassword(input.password());
        var member = new Member();
        member.username = input.username().toLowerCase(Locale.ROOT);
        member.nickname = member.username;
        member.bio = "";
        member.passwordHash = passwords.encode(input.password());
        members.insert(member);
        return new Token(tokens.issue(member.id));
    }

    public Token login(Credentials input) {
        checkPassword(input.password());
        Member member = members.selectOne(Wrappers.<Member>query().eq("username", input.username().toLowerCase(Locale.ROOT)));
        boolean matches = passwords.matches(input.password(), member == null ? unknownUserHash : member.passwordHash);
        if (member == null || !matches) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号或密码错误");
        return new Token(tokens.issue(member.id));
    }

    public Profile profile(long id) {
        Member member = members.selectById(id);
        if (member == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号不存在");
        return new Profile(member);
    }

    public Profile update(long id, ProfileInput input) {
        members.update(null, Wrappers.<Member>update().eq("id", id)
                .set("nickname", input.nickname().strip()).set("bio", input.bio().strip()));
        return profile(id);
    }

    private void checkPassword(String password) {
        // BCrypt 按字节截断，限制 UTF-8 长度以避免不同密码被视为相同。
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "密码最多 72 字节");
        }
    }
}
