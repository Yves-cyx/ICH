package com.yves.ich.navigation;

import android.app.Activity;
import android.content.Intent;

import com.yves.ich.feature.catalog.CatalogDetailActivity;
import com.yves.ich.feature.drawing.DrawingActivity;
import com.yves.ich.feature.login.LoginActivity;
import com.yves.ich.feature.profile.FavoritesActivity;
import com.yves.ich.feature.profile.SettingsActivity;
import com.yves.ich.feature.profile.WorksActivity;
import com.yves.ich.feature.stage.StageActivity;
import com.yves.ich.feature.viewer.ViewerActivity;

public final class AppNavigator {
    public static final String HERITAGE_ID = "heritageId";
    public static final String ASSET_ID = "assetId";
    public static final String TEMPLATE_ID = "templateId";
    public static final String WORK_ID = "workId";

    private AppNavigator() {}

    public static void openCatalogDetail(Activity from, String heritageId) {
        from.startActivity(new Intent(from, CatalogDetailActivity.class).putExtra(HERITAGE_ID, heritageId));
    }

    public static void openStage(Activity from) {
        from.startActivity(new Intent(from, StageActivity.class));
    }

    public static void openViewer(Activity from, String assetId) {
        from.startActivity(new Intent(from, ViewerActivity.class).putExtra(ASSET_ID, assetId));
    }

    public static void openDrawing(Activity from, String templateId) {
        from.startActivity(new Intent(from, DrawingActivity.class).putExtra(TEMPLATE_ID, templateId));
    }

    public static void openWork(Activity from, String workId) {
        from.startActivity(new Intent(from, DrawingActivity.class).putExtra(WORK_ID, workId));
    }

    public static void openLogin(Activity from) {
        from.startActivity(new Intent(from, LoginActivity.class));
    }

    public static void openFavorites(Activity from) {
        from.startActivity(new Intent(from, FavoritesActivity.class));
    }

    public static void openWorks(Activity from) {
        from.startActivity(new Intent(from, WorksActivity.class));
    }

    public static void openSettings(Activity from) {
        from.startActivity(new Intent(from, SettingsActivity.class));
    }
}
