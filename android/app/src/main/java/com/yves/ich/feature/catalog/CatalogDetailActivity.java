package com.yves.ich.feature.catalog;

import android.os.Bundle;
import android.widget.TextView;

import com.yves.ich.R;
import com.yves.ich.navigation.AppNavigator;
import com.yves.ich.ui.FeatureActivity;

public class CatalogDetailActivity extends FeatureActivity {
    @Override
    protected int contentLayout() {
        return R.layout.activity_catalog_detail;
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        TextView argument = findViewById(R.id.catalog_detail_argument);
        String heritageId = readId(AppNavigator.HERITAGE_ID);
        argument.setText(heritageId.isEmpty()
                ? getString(R.string.catalog_detail_missing)
                : getString(R.string.catalog_detail_id, heritageId));
    }
}
