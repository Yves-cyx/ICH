package com.yingjian.api.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public final class Models {
    private Models() {}

    @TableName("member")
    public static class Member {
        @TableId(type = IdType.AUTO) public Long id;
        public String username;
        public String passwordHash;
        public String nickname;
        public String bio;
    }

    @TableName("heritage")
    public static class Heritage {
        @TableId public Long id;
        public String title;
        public String category;
        public String region;
        public String summary;
        public String description;
        public String artKey;
    }

    @TableName("puppet_work")
    public static class Work {
        @TableId(type = IdType.AUTO) public Long id;
        public Long ownerId;
        public String name;
        public String shape;
        public String palette;
        public BigDecimal x;
        public BigDecimal y;
        public BigDecimal angle;
    }

    public record Credentials(
            @NotBlank @Pattern(regexp = "[A-Za-z0-9_]{3,24}", message = "账号须为 3 至 24 位字母、数字或下划线") String username,
            @NotBlank @Size(min = 8, max = 72, message = "密码长度须为 8 至 72 个字符") String password) {}

    public record ProfileInput(
            @NotBlank @Size(max = 24) String nickname,
            @NotNull @Size(max = 160) String bio) {}

    public record WorkInput(
            @NotBlank @Size(max = 32) String name,
            @NotBlank @Pattern(regexp = "scholar|warrior|lady") String shape,
            @NotBlank @Pattern(regexp = "cinnabar|indigo|amber") String palette,
            @NotNull @DecimalMin("0.08") @DecimalMax("0.92") BigDecimal x,
            @NotNull @DecimalMin("0.22") @DecimalMax("0.78") BigDecimal y,
            @NotNull @DecimalMin("-45") @DecimalMax("45") BigDecimal angle) {}

    public record Profile(String username, String nickname, String bio) {
        public Profile(Member member) { this(member.username, member.nickname, member.bio); }
    }

    public record Token(String token) {}

    public record WorkCard(Long id, String name, String shape, String palette,
                           BigDecimal x, BigDecimal y, BigDecimal angle) {
        public WorkCard(Work work) {
            this(work.id, work.name, work.shape, work.palette, work.x, work.y, work.angle);
        }
    }

    public record Envelope<T>(int code, String message, T data) {
        public static <T> Envelope<T> ok(T data) { return new Envelope<>(0, "成功", data); }
        public static Envelope<Void> error(int code, String message) {
            return new Envelope<>(code, message, null);
        }
    }
}
