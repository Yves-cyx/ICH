package com.yves.ich.feature.login;

import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.yves.ich.R;
import com.yves.ich.ui.FeatureActivity;

public class LoginActivity extends FeatureActivity {
    private EditText account;
    private EditText password;
    private TextView status;

    @Override
    protected int contentLayout() {
        return R.layout.activity_login;
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        account = findViewById(R.id.login_account);
        password = findViewById(R.id.login_password);
        status = findViewById(R.id.login_status);
        findViewById(R.id.login_submit).setOnClickListener(view -> submit());
        password.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit();
                return true;
            }
            return false;
        });
    }

    private void submit() {
        status.setText(null);
        if (account.getText().toString().trim().isEmpty()) {
            account.setError(getString(R.string.login_missing_account));
            account.requestFocus();
            return;
        }
        if (password.length() == 0) {
            password.setError(getString(R.string.login_missing_password));
            password.requestFocus();
            return;
        }
        // Connect AuthRepository here when T06 is available.
        status.setText(R.string.login_unavailable);
    }
}
