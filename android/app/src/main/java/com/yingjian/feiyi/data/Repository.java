package com.yingjian.feiyi.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import androidx.room.Room;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.yingjian.feiyi.BuildConfig;
import com.yingjian.feiyi.data.Models.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class Repository implements AutoCloseable {
    private final SessionStore sessions;
    private final HeritageDatabase cache;
    private final FeiyiApi api;
    private final Gson gson = new Gson();
    private final SharedPreferences favorites;

    public Repository(Context context) {
        sessions = new SessionStore(context);
        favorites = context.getSharedPreferences("catalog_favorites", Context.MODE_PRIVATE);
        cache = Room.databaseBuilder(context, HeritageDatabase.class, "heritage.db")
                .addMigrations(HeritageDatabase.ADD_COVER_URL).build();
        OkHttpClient http = new OkHttpClient.Builder().callTimeout(10, TimeUnit.SECONDS).build();
        api = new Retrofit.Builder().baseUrl(BuildConfig.API_URL).client(http)
                .addConverterFactory(GsonConverterFactory.create(gson)).build().create(FeiyiApi.class);
    }

    public String session() { return sessions.load(); }

    public String authenticate(String username, String password, boolean register) throws IOException, GeneralSecurityException {
        Credentials input = new Credentials(username, password);
        Token token = require(register ? api.register(input) : api.login(input));
        sessions.save(token.token);
        return token.token;
    }

    public void signOut() { sessions.clear(); }

    public List<Heritage> cachedCatalog() { return cache.catalog().read(); }

    public List<Heritage> refreshCatalog() throws IOException {
        List<Heritage> entries = require(api.catalog(null, null, null, null));
        // 更新整个公共目录，读者不会看到被清空的中间态。
        cache.runInTransaction(() -> {
            cache.catalog().clear();
            cache.catalog().write(entries);
        });
        return entries;
    }

    public HeritagePage searchCatalog(String keyword, String category, Integer page, Integer size) throws IOException {
        // 搜索只返回匹配结果，避免用筛选后的列表覆盖整个离线目录。
        return readPage(api.catalog(keyword, category, page, size));
    }

    public HeritagePage listFavorites(String token, Integer page, Integer size) throws IOException {
        return readPage(api.favorites(bearer(token), page, size));
    }

    public void addFavorite(String token, long heritageId) throws IOException {
        require(api.addFavorite(bearer(token), heritageId));
    }

    public void removeFavorite(String token, long heritageId) throws IOException {
        require(api.removeFavorite(bearer(token), heritageId));
    }

    public Heritage cachedDetail(long id) { return cache.catalog().detail(id); }
    public Heritage detail(long id) throws IOException {
        Heritage entry = require(api.detail(id));
        cache.catalog().writeDetail(entry);
        return entry;
    }

    public String localFavoriteAccount(String token) {
        // The subject only namespaces device-local preferences, never authorizes a request.
        try {
            String[] jwt = token.split("\\.", -1);
            if (jwt.length != 3) return "";
            byte[] payload = Base64.decode(jwt[1], Base64.URL_SAFE | Base64.NO_WRAP);
            JsonObject claims = gson.fromJson(new String(payload, StandardCharsets.UTF_8), JsonObject.class);
            String subject = claims.get("sub").getAsString();
            return Long.parseLong(subject) > 0 ? BuildConfig.API_URL + "#" + subject : "";
        } catch (RuntimeException invalidToken) {
            return "";
        }
    }

    public boolean isLocalFavorite(String account, long id) {
        return !account.isEmpty() && favorites.getBoolean(account + "/" + id, false);
    }

    public void setLocalFavorite(String account, long id, boolean selected) {
        String key = account + "/" + id;
        SharedPreferences.Editor editor = favorites.edit();
        if (selected) editor.putBoolean(key, true);
        else editor.remove(key);
        editor.apply();
    }
    public Profile profile(String token) throws IOException { return require(api.profile(bearer(token))); }
    public Profile saveProfile(String token, Profile input) throws IOException { return require(api.saveProfile(bearer(token), input)); }
    public List<Work> works(String token) throws IOException { return require(api.works(bearer(token))); }
    public Work saveWork(String token, Work work) throws IOException {
        return require(work.id == 0 ? api.create(bearer(token), work) : api.edit(bearer(token), work.id, work));
    }
    public void deleteWork(String token, long id) throws IOException { require(api.delete(bearer(token), id)); }

    private String bearer(String token) { return "Bearer " + token; }

    private HeritagePage readPage(Call<Envelope<List<Heritage>>> call) throws IOException {
        Response<Envelope<List<Heritage>>> response = call.execute();
        List<Heritage> items = require(response);
        String totalCount = response.headers().get("X-Total-Count");
        return new HeritagePage(items, totalCount == null ? null : Long.valueOf(totalCount));
    }

    private <T> T require(Call<Envelope<T>> call) throws IOException {
        return require(call.execute());
    }

    private <T> T require(Response<Envelope<T>> response) throws IOException {
        Envelope<T> body = response.body();
        if (response.isSuccessful() && body != null && body.code == 0) return body.data;
        String message = "请求失败，请稍后再试";
        try (ResponseBody error = response.errorBody()) {
            if (error != null) {
                Envelope<?> data = gson.fromJson(error.charStream(), Envelope.class);
                if (data != null && data.message != null) message = data.message;
            }
        } catch (RuntimeException ignored) {
            // 网关返回 HTML 等非 JSON 内容时使用默认提示。
        }
        throw new Failure(response.code(), message);
    }

    @Override public void close() { cache.close(); }

    public static final class Failure extends IOException {
        public final int status;
        Failure(int status, String message) { super(message); this.status = status; }
    }
}
