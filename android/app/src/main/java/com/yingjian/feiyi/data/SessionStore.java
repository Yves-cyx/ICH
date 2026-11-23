package com.yingjian.feiyi.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

final class SessionStore {
    private static final String ALIAS = "feiyi-session-v1";
    private final SharedPreferences preferences;

    SessionStore(Context context) {
        preferences = context.getSharedPreferences("session", Context.MODE_PRIVATE);
    }

    String load() {
        String encrypted = preferences.getString("payload", "");
        if (encrypted.isEmpty()) return "";
        try {
            String[] parts = encrypted.split("\\.", 2);
            Cipher cipher = cipher();
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(128, decode(parts[0])));
            return new String(cipher.doFinal(decode(parts[1])), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException | IndexOutOfBoundsException failure) {
            clear();
            return "";
        }
    }

    void save(String token) throws GeneralSecurityException {
        Cipher cipher = cipher();
        cipher.init(Cipher.ENCRYPT_MODE, key());
        byte[] bytes = cipher.doFinal(token.getBytes(StandardCharsets.UTF_8));
        preferences.edit().putString("payload", encode(cipher.getIV()) + "." + encode(bytes)).apply();
    }

    void clear() { preferences.edit().remove("payload").apply(); }

    private Cipher cipher() throws GeneralSecurityException { return Cipher.getInstance("AES/GCM/NoPadding"); }

    private SecretKey key() throws GeneralSecurityException {
        KeyStore store = KeyStore.getInstance("AndroidKeyStore");
        try { store.load(null); } catch (java.io.IOException error) { throw new GeneralSecurityException(error); }
        if (store.getKey(ALIAS, null) instanceof SecretKey existing) return existing;
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());
        return generator.generateKey();
    }

    private byte[] decode(String value) { return Base64.decode(value, Base64.NO_WRAP); }
    private String encode(byte[] value) { return Base64.encodeToString(value, Base64.NO_WRAP); }
}
