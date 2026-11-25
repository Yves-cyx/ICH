package com.yingjian.feiyi.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

public final class Models {
    private Models() {}

    public static class Envelope<T> {
        public int code;
        public String message;
        public T data;
    }

    public static class Credentials {
        public final String username;
        public final String password;
        public Credentials(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    public static class Token { public String token; }

    @Entity(tableName = "heritage")
    public static class Heritage {
        @PrimaryKey public long id;
        public String title;
        public String category;
        public String region;
        public String summary;
        public String description;
        public String artKey;
        public String coverUrl;
    }

    public static class Profile {
        public String username;
        public String nickname;
        public String bio;
        public Profile(String nickname, String bio) { this.nickname = nickname; this.bio = bio; }
    }

    public static class Work {
        public long id;
        public String name = "";
        public String shape = "scholar";
        public String palette = "cinnabar";
        public float x = .50f;
        public float y = .54f;
        public float angle = 0f;

        public Work copy() {
            Work duplicate = new Work();
            duplicate.id = id;
            duplicate.name = name;
            duplicate.shape = shape;
            duplicate.palette = palette;
            duplicate.x = x;
            duplicate.y = y;
            duplicate.angle = angle;
            return duplicate;
        }
    }
}
