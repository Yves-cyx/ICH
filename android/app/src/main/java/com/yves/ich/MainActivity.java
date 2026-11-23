package com.yves.ich;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.yves.ich.navigation.AppNavigator;

/** A owns the app shell. Each feature replaces its own included layout. */
public class MainActivity extends Activity {
    private static final String TAB_KEY = "selected_tab";
    private final int[] pages = {R.id.catalog_page, R.id.experience_page, R.id.profile_page};
    private final int[] tabs = {R.id.tab_catalog, R.id.tab_experience, R.id.tab_profile};
    private int selectedTab;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        View root = findViewById(R.id.app_root);
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });
        root.requestApplyInsets();
        for (int i = 0; i < tabs.length; i++) {
            final int index = i;
            findViewById(tabs[i]).setOnClickListener(view -> selectTab(index));
        }
        bindPageLinks();
        selectTab(state == null ? 0 : state.getInt(TAB_KEY, 0));
    }

    private void bindPageLinks() {
        findViewById(R.id.open_catalog_detail).setOnClickListener(
                view -> AppNavigator.openCatalogDetail(this, "shadow-puppetry"));
        findViewById(R.id.open_stage).setOnClickListener(
                view -> AppNavigator.openStage(this));
        findViewById(R.id.open_viewer).setOnClickListener(
                view -> AppNavigator.openViewer(this, "shadow-figure"));
        findViewById(R.id.open_drawing).setOnClickListener(
                view -> AppNavigator.openDrawing(this, "paper-cutting"));
        findViewById(R.id.open_login).setOnClickListener(
                view -> AppNavigator.openLogin(this));
        findViewById(R.id.open_favorites).setOnClickListener(
                view -> AppNavigator.openFavorites(this));
        findViewById(R.id.open_works).setOnClickListener(
                view -> AppNavigator.openWorks(this));
        findViewById(R.id.open_settings).setOnClickListener(
                view -> AppNavigator.openSettings(this));
    }

    private void selectTab(int index) {
        selectedTab = Math.max(0, Math.min(index, pages.length - 1));
        for (int i = 0; i < pages.length; i++) {
            findViewById(pages[i]).setVisibility(i == selectedTab ? View.VISIBLE : View.GONE);
            Button tab = findViewById(tabs[i]);
            tab.setTextColor(getColor(i == selectedTab ? R.color.ich_red : R.color.ich_ink));
            tab.setSelected(i == selectedTab);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        state.putInt(TAB_KEY, selectedTab);
        super.onSaveInstanceState(state);
    }
}
