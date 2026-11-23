package com.yves.ich.feature.viewer;

import android.os.Bundle;
import android.widget.TextView;

import com.yves.ich.R;
import com.yves.ich.navigation.AppNavigator;
import com.yves.ich.ui.FeatureActivity;

public class ViewerActivity extends FeatureActivity {
    @Override
    protected int contentLayout() {
        return R.layout.activity_viewer;
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        TextView argument = findViewById(R.id.viewer_argument);
        String assetId = readId(AppNavigator.ASSET_ID);
        argument.setText(assetId.isEmpty()
                ? getString(R.string.viewer_missing)
                : getString(R.string.viewer_id, assetId));
    }
}
