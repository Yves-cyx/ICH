package com.yves.ich.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yves.ich.R;

/** Common header and back action for the eight secondary pages. */
public abstract class FeatureActivity extends Activity {
    protected abstract int contentLayout();

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_feature);
        TextView heading = findViewById(R.id.feature_heading);
        heading.setText(getTitle());
        findViewById(R.id.feature_back).setOnClickListener(view -> finish());
        LinearLayout content = findViewById(R.id.feature_content);
        getLayoutInflater().inflate(contentLayout(), content, true);
    }

    protected String readId(String key) {
        String id = getIntent().getStringExtra(key);
        return id == null ? "" : id.trim();
    }
}
