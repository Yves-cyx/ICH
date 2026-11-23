package com.yves.ich.feature.drawing;

import android.os.Bundle;
import android.widget.TextView;

import com.yves.ich.R;
import com.yves.ich.navigation.AppNavigator;
import com.yves.ich.ui.FeatureActivity;

public class DrawingActivity extends FeatureActivity {
    @Override
    protected int contentLayout() {
        return R.layout.activity_drawing;
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        TextView argument = findViewById(R.id.drawing_argument);
        String workId = readId(AppNavigator.WORK_ID);
        String templateId = readId(AppNavigator.TEMPLATE_ID);
        if (!workId.isEmpty()) {
            argument.setText(getString(R.string.drawing_work_id, workId));
        } else if (!templateId.isEmpty()) {
            argument.setText(getString(R.string.drawing_id, templateId));
        } else {
            argument.setText(R.string.drawing_missing);
        }
    }
}
