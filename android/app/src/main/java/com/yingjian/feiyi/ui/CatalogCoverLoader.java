package com.yingjian.feiyi.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.AtomicFile;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import com.yingjian.feiyi.BuildConfig;
import com.yingjian.feiyi.R;
import com.yingjian.feiyi.data.Models.Heritage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.ByteString;

final class CatalogCoverLoader {
    private static final int MAX_FILE_BYTES = 8 * 1024 * 1024;
    private static final int MAX_BITMAP_EDGE = 1200;
    private static CatalogCoverLoader instance;

    private final File directory;
    private final HttpUrl baseUrl = HttpUrl.get(BuildConfig.API_URL);
    private final Handler main = new Handler(Looper.getMainLooper());
    private final OkHttpClient http = new OkHttpClient.Builder().callTimeout(12, TimeUnit.SECONDS).build();
    private final ExecutorService workers = Executors.newFixedThreadPool(2);
    private final Map<HttpUrl, List<WeakReference<ImageView>>> pending = new HashMap<>();
    private final LruCache<HttpUrl, Bitmap> memory = new LruCache<>(16 * 1024 * 1024) {
        @Override protected int sizeOf(HttpUrl key, Bitmap value) { return value.getAllocationByteCount(); }
    };

    static CatalogCoverLoader get(Context context) {
        if (instance == null) instance = new CatalogCoverLoader(context.getApplicationContext());
        return instance;
    }

    private CatalogCoverLoader(Context context) {
        directory = new File(context.getNoBackupFilesDir(), "catalog-covers");
    }

    void bind(ImageView image, ShadowArtView art, Heritage entry) {
        HttpUrl url = entry == null || entry.coverUrl == null || entry.coverUrl.trim().isEmpty()
                ? null : baseUrl.resolve(entry.coverUrl.trim());
        if (url != null && url.equals(image.getTag())) return;

        image.setTag(url);
        boolean hasArtwork = entry != null && entry.artKey != null && !entry.artKey.trim().isEmpty();
        if (hasArtwork) art.setArtwork(entry.artKey);
        art.setVisibility(hasArtwork ? View.VISIBLE : View.GONE);
        image.setScaleType(ImageView.ScaleType.CENTER);
        image.setImageResource(R.drawable.ic_catalog_placeholder);
        image.setVisibility(hasArtwork ? View.GONE : View.VISIBLE);
        if (url == null) return;

        Bitmap bitmap = memory.get(url);
        if (bitmap != null) {
            display(image, bitmap);
            return;
        }
        List<WeakReference<ImageView>> waiting = pending.get(url);
        if (waiting == null) {
            waiting = new ArrayList<>();
            pending.put(url, waiting);
            workers.execute(() -> {
                Bitmap loaded = null;
                try {
                    loaded = load(url);
                } catch (IOException ignored) {
                    // The built-in artwork stays visible and the next bind may retry.
                }
                Bitmap result = loaded;
                main.post(() -> deliver(url, result));
            });
        }
        waiting.add(new WeakReference<>(image));
    }

    private void deliver(HttpUrl url, Bitmap bitmap) {
        List<WeakReference<ImageView>> targets = pending.remove(url);
        if (bitmap != null) memory.put(url, bitmap);
        for (WeakReference<ImageView> target : targets) {
            ImageView image = target.get();
            if (image == null || !url.equals(image.getTag())) continue;
            if (bitmap != null) display(image, bitmap);
            else image.setTag(null);
        }
    }

    private void display(ImageView image, Bitmap bitmap) {
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setImageBitmap(bitmap);
        image.setVisibility(View.VISIBLE);
    }

    private Bitmap load(HttpUrl url) throws IOException {
        String filename = ByteString.encodeUtf8(url.toString()).sha256().hex();
        File saved = new File(directory, filename);
        Bitmap fromDisk = decode(saved);
        if (fromDisk != null) return fromDisk;

        try (Response response = http.newCall(new Request.Builder().url(url).build()).execute()) {
            ResponseBody body = response.body();
            if (!response.isSuccessful() || body == null || body.contentLength() > MAX_FILE_BYTES) return null;
            AtomicFile file = new AtomicFile(saved);
            FileOutputStream output = file.startWrite();
            try (InputStream input = body.byteStream()) {
                byte[] chunk = new byte[8192];
                int total = 0;
                int length;
                while ((length = input.read(chunk)) != -1) {
                    total += length;
                    if (total > MAX_FILE_BYTES) throw new IOException("Cover exceeds size limit");
                    output.write(chunk, 0, length);
                }
                file.finishWrite(output);
            } catch (IOException failure) {
                file.failWrite(output);
                throw failure;
            }
            return decode(saved);
        }
    }

    private Bitmap decode(File file) {
        if (!file.isFile()) return null;
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getPath(), bounds);
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        int edge = Math.max(bounds.outWidth, bounds.outHeight);
        while (edge / options.inSampleSize > MAX_BITMAP_EDGE) options.inSampleSize *= 2;
        return BitmapFactory.decodeFile(file.getPath(), options);
    }
}
