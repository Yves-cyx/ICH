package com.yingjian.feiyi.ui;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.yingjian.feiyi.data.Models.*;
import com.yingjian.feiyi.data.Repository;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public final class FeiyiViewModel extends AndroidViewModel {
    final MutableLiveData<Boolean> signedIn = new MutableLiveData<>(false);
    final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
    final MutableLiveData<String> authError = new MutableLiveData<>();
    final MutableLiveData<String> message = new MutableLiveData<>();
    final MutableLiveData<String> catalogNote = new MutableLiveData<>();
    final MutableLiveData<String> detailNote = new MutableLiveData<>();
    final MutableLiveData<String> profileNote = new MutableLiveData<>();
    final MutableLiveData<List<Heritage>> catalog = new MutableLiveData<>(Collections.emptyList());
    final MutableLiveData<Heritage> detail = new MutableLiveData<>();
    final MutableLiveData<Profile> profile = new MutableLiveData<>();
    final MutableLiveData<List<Work>> works = new MutableLiveData<>(Collections.emptyList());
    final MutableLiveData<Work> savedWork = new MutableLiveData<>();
    Work draft = new Work();

    private final Repository repository;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private String session;
    private int generation;
    private int pending;
    private long openDetail;

    public FeiyiViewModel(@NonNull Application app) {
        super(app);
        repository = new Repository(app);
        session = repository.session();
        signedIn.setValue(!session.isEmpty());
    }

    void authenticate(String username, String password, boolean register) {
        if (Boolean.TRUE.equals(busy.getValue())) return;
        authError.setValue(null);
        if (!username.matches("[A-Za-z0-9_]{3,24}") || password.length() < 8) {
            authError.setValue("请输入 3 至 24 位字母、数字或下划线账号，以及至少 8 位的密码。");
            return;
        }
        execute(() -> repository.authenticate(username, password, register), token -> {
            session = token;
            signedIn.setValue(true);
        }, error -> authError.setValue(explain(error)));
    }

    void logout() {
        generation++;
        session = "";
        repository.signOut();
        profile.setValue(null);
        works.setValue(Collections.emptyList());
        draft = new Work();
        signedIn.setValue(false);
    }

    void loadCatalog() {
        int requestGeneration = generation;
        catalogNote.setValue(null);
        execute(() -> {
            var cached = repository.cachedCatalog();
            if (!cached.isEmpty()) main.post(() -> {
                if (generation == requestGeneration) catalog.setValue(cached);
            });
            return repository.refreshCatalog();
        }, entries -> {
            catalog.setValue(entries);
            if (entries.isEmpty()) catalogNote.setValue("目录暂时没有内容");
        }, error -> catalogNote.setValue(catalog.getValue().isEmpty()
                ? explain(error) : "离线浏览 · 正在显示上次加载的目录"));
    }

    void openHeritage(long id) {
        openDetail = id;
        detail.setValue(null);
        detailNote.setValue(null);
        int requestGeneration = generation;
        execute(() -> {
            Heritage cached = repository.cachedDetail(id);
            if (cached != null) main.post(() -> publishDetail(requestGeneration, id, cached));
            return repository.detail(id);
        }, entry -> publishDetail(requestGeneration, id, entry), error -> {
            if (openDetail == id && detail.getValue() == null) detailNote.setValue(explain(error));
        });
    }

    private void publishDetail(int requestGeneration, long id, Heritage entry) {
        if (generation == requestGeneration && openDetail == id) detail.setValue(entry);
    }

    void loadProfile() {
        if (session.isEmpty()) return;
        profileNote.setValue(null);
        String token = session;
        execute(() -> repository.profile(token), profile::setValue, this::handlePrivateError);
        execute(() -> repository.works(token), works::setValue, this::handlePrivateError);
    }

    void saveProfile(String nickname, String bio) {
        String token = session;
        execute(() -> repository.saveProfile(token, new Profile(nickname, bio)), item -> {
            profile.setValue(item);
            message.setValue("资料已保存");
        }, this::handlePrivateError);
    }

    void saveWork() {
        if (Boolean.TRUE.equals(busy.getValue())) return;
        if (draft.name.trim().isEmpty()) {
            message.setValue("请先为作品取个名字");
            return;
        }
        String token = session;
        Work activeDraft = draft;
        Work snapshot = activeDraft.copy();
        execute(() -> repository.saveWork(token, snapshot), result -> {
            if (draft == activeDraft) draft.id = result.id;
            savedWork.setValue(result);
            message.setValue("作品已保存，可以在「我的」查看");
        }, this::handlePrivateError);
    }

    void deleteWork(Work work) {
        String token = session;
        execute(() -> {
            repository.deleteWork(token, work.id);
            return repository.works(token);
        }, items -> {
            if (draft.id == work.id) draft = new Work();
            works.setValue(items);
            message.setValue("作品已删除");
        }, this::handlePrivateError);
    }

    private void handlePrivateError(Exception error) {
        if (error instanceof Repository.Failure failure && failure.status == 401) {
            logout();
            authError.setValue("登录已过期，请重新登录");
        } else {
            profileNote.setValue(explain(error));
            message.setValue(explain(error));
        }
    }

    private String explain(Exception error) {
        if (error instanceof Repository.Failure) return error.getMessage();
        if (error instanceof IOException) return "无法连接服务，请检查网络或稍后重试";
        return "暂时无法完成操作，请重试";
    }

    private <T> void execute(Task<T> work, Consumer<T> success, Consumer<Exception> failure) {
        int requestGeneration = generation;
        pending++;
        busy.setValue(true);
        worker.execute(() -> {
            try {
                T result = work.run();
                main.post(() -> finish(requestGeneration, () -> success.accept(result)));
            } catch (Exception error) {
                main.post(() -> finish(requestGeneration, () -> failure.accept(error)));
            }
        });
    }

    private void finish(int requestGeneration, Runnable deliver) {
        pending--;
        busy.setValue(pending > 0);
        if (requestGeneration == generation) deliver.run();
    }

    @Override protected void onCleared() {
        worker.execute(repository::close);
        worker.shutdown();
        super.onCleared();
    }

    private interface Task<T> { T run() throws Exception; }
}
