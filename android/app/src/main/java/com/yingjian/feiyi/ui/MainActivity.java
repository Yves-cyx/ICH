package com.yingjian.feiyi.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.yingjian.feiyi.R;
import com.yingjian.feiyi.data.Models.*;
import com.yingjian.feiyi.databinding.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MainActivity extends AppCompatActivity {
    private static final int CATALOG_SLOTS = 50;
    private enum Experience { STAGE, GALLERY, CREATE }

    private ActivityMainBinding shell;
    private FeiyiViewModel model;
    private PageAuthBinding auth;
    private PageDiscoverBinding discover;
    private PageStageBinding stage;
    private PageProfileBinding profile;
    private PageDetailBinding detail;
    private CatalogAdapter catalogCards;
    private CatalogCoverLoader catalogCovers;
    private WorksAdapter workCards;
    private boolean registering;
    private boolean lit;
    private boolean updatingStage;
    private boolean catalogRequested;
    private int tab = R.id.tab_discover;
    private String catalogQuery = "";
    private Parcelable catalogPosition;
    private long detailId;
    private Experience experience = Experience.STAGE;
    private final Work galleryFigure = new Work();

    @Override protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        shell = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(shell.getRoot());
        model = new ViewModelProvider(this).get(FeiyiViewModel.class);
        catalogCovers = CatalogCoverLoader.get(this);
        if (savedState != null) {
            tab = savedState.getInt("tab", R.id.tab_discover);
            catalogQuery = savedState.getString("catalogQuery", "");
            catalogPosition = savedState.getParcelable("catalogPosition");
            detailId = savedState.getLong("detail");
            registering = savedState.getBoolean("register");
            lit = savedState.getBoolean("lit");
            experience = Experience.valueOf(savedState.getString("experience", Experience.STAGE.name()));
        }
        shell.navigation.setOnItemSelectedListener(item -> {
            detailId = 0;
            int selected = item.getItemId();
            if (selected == R.id.tab_stage && tab != selected) experience = Experience.STAGE;
            tab = selected;
            showTab();
            return true;
        });
        shell.navigation.setOnItemReselectedListener(item -> {
            if (detailId != 0) {
                detailId = 0;
                showTab();
            }
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (detailId != 0) {
                    detailId = 0;
                    showTab();
                } else if (auth != null && registering) {
                    setRegisterMode(false);
                } else if (Boolean.TRUE.equals(model.signedIn.getValue()) && tab != R.id.tab_discover) {
                    shell.navigation.setSelectedItemId(R.id.tab_discover);
                } else {
                    finish();
                }
            }
        });
        observeModel();
    }

    private void observeModel() {
        model.signedIn.observe(this, signedIn -> {
            if (!signedIn) {
                catalogRequested = false;
                detailId = 0;
                tab = R.id.tab_discover;
                showAuth();
                catalogQuery = "";
                catalogPosition = null;
                return;
            }
            if (detailId != 0) showDetail(detailId);
            else {
                if (shell.navigation.getSelectedItemId() == tab) showTab();
                else shell.navigation.setSelectedItemId(tab);
            }
        });
        model.busy.observe(this, busy -> {
            visible(shell.progress, busy);
            if (auth != null) auth.submit.setEnabled(!busy);
            if (stage != null) stage.saveWork.setEnabled(!busy);
        });
        model.authError.observe(this, error -> {
            if (auth != null) showText(auth.error, error);
        });
        model.catalog.observe(this, ignored -> renderCatalog());
        model.catalogNote.observe(this, ignored -> renderCatalog());
        model.detail.observe(this, ignored -> renderDetail());
        model.detailNote.observe(this, ignored -> renderDetail());
        model.profile.observe(this, ignored -> renderProfile());
        model.works.observe(this, ignored -> renderProfile());
        model.profileNote.observe(this, ignored -> renderProfile());
        model.message.observe(this, message -> {
            if (message == null) return;
            Snackbar reminder = Snackbar.make(shell.content, message, Snackbar.LENGTH_LONG);
            if (shell.navigation.isShown()) reminder.setAnchorView(shell.navigation);
            reminder.show();
            model.message.setValue(null);
        });
        model.savedWork.observe(this, saved -> {
            if (saved == null) return;
            renderStage();
            model.savedWork.setValue(null);
        });
    }

    private void showAuth() {
        clearPage();
        visible(shell.toolbar, false);
        visible(shell.navigation, false);
        auth = PageAuthBinding.inflate(getLayoutInflater(), shell.content, false);
        Work poster = new Work();
        poster.name = "host";
        poster.x = .37f;
        auth.hero.showWork(poster, true, false);
        setRegisterMode(registering);
        auth.switchMode.setOnClickListener(view -> {
            model.authError.setValue(null);
            setRegisterMode(!registering);
        });
        auth.submit.setOnClickListener(view -> {
            hideKeyboard();
            model.authenticate(value(auth.username).trim(), value(auth.password), registering);
        });
        showText(auth.error, model.authError.getValue());
        auth.submit.setEnabled(!Boolean.TRUE.equals(model.busy.getValue()));
        replace(auth.getRoot(), false);
    }

    private void setRegisterMode(boolean enabled) {
        registering = enabled;
        auth.submit.setText(enabled ? R.string.register : R.string.login);
        auth.switchMode.setText(enabled ? R.string.back_to_login : R.string.register);
    }

    private void showTab() {
        clearPage();
        visible(shell.toolbar, true);
        visible(shell.navigation, true);
        shell.toolbar.setNavigationIcon(null);
        shell.toolbar.setTitle(R.string.app_name);
        if (tab == R.id.tab_stage) {
            shell.toolbar.setTitle(R.string.experience);
            showExperience();
        } else if (tab == R.id.tab_profile) {
            shell.toolbar.setTitle(R.string.profile);
            showProfile();
        } else {
            showDiscover();
        }
    }

    private void showDiscover() {
        visible(shell.toolbar, false);
        discover = PageDiscoverBinding.inflate(getLayoutInflater(), shell.content, false);
        catalogCards = new CatalogAdapter(catalogCovers, entry -> {
            hideKeyboard();
            showDetail(entry.id);
        });
        int spans = getResources().getConfiguration().screenWidthDp >= 600 ? 3 : 2;
        discover.items.setLayoutManager(new GridLayoutManager(this, spans));
        discover.items.setAdapter(catalogCards);
        discover.searchQuery.setText(catalogQuery);
        discover.searchQuery.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence text, int start, int before, int count) {
                catalogQuery = text.toString();
                catalogPosition = null;
                discover.items.scrollToPosition(0);
                renderCatalog();
            }
            @Override public void afterTextChanged(Editable text) {}
        });
        discover.searchQuery.setOnEditorActionListener((view, action, event) -> {
            if (action != EditorInfo.IME_ACTION_SEARCH) return false;
            hideKeyboard();
            discover.getRoot().requestFocus();
            return true;
        });
        discover.clearSearch.setOnClickListener(view -> discover.searchQuery.setText(""));
        discover.retry.setOnClickListener(view -> model.loadCatalog());
        replace(discover.getRoot(), true);
        discover.getRoot().requestFocus();
        renderCatalog();
        if (!catalogRequested) {
            catalogRequested = true;
            model.loadCatalog();
        }
    }

    private void renderCatalog() {
        if (discover == null) return;
        boolean searching = !CatalogSearch.normalize(catalogQuery).isEmpty();
        List<Heritage> matches = CatalogSearch.match(model.catalog.getValue(), catalogQuery);
        String note = model.catalogNote.getValue();
        boolean catalogReady = !model.catalog.getValue().isEmpty() || note != null;
        List<CatalogAdapter.Slot> cards = new ArrayList<>();
        for (Heritage entry : matches) cards.add(new CatalogAdapter.Slot(entry.id, entry));
        if (!searching && catalogReady) {
            for (int slot = cards.size(); slot < CATALOG_SLOTS; slot++) {
                cards.add(new CatalogAdapter.Slot(-1L - slot, null));
            }
        }
        boolean firstContent = !matches.isEmpty()
                && catalogCards.getCurrentList().stream().noneMatch(card -> card.entry() != null);
        PageDiscoverBinding page = discover;
        catalogCards.submitList(cards, () -> {
            if (discover != page) return;
            if (catalogPosition != null && !matches.isEmpty()) {
                page.items.getLayoutManager().onRestoreInstanceState(catalogPosition);
                catalogPosition = null;
            } else if (firstContent) {
                page.items.scrollToPosition(0);
            }
        });
        page.count.setText(getString(searching ? R.string.catalog_search_count : R.string.catalog_count, matches.size()));
        visible(page.clearSearch, !catalogQuery.isEmpty());
        if (searching && matches.isEmpty() && !model.catalog.getValue().isEmpty()) {
            note = getString(R.string.catalog_empty_search);
        }
        showText(page.message, note);
        visible(page.retry, model.catalogNote.getValue() != null);
    }

    private void showDetail(long id) {
        clearPage();
        detailId = id;
        visible(shell.toolbar, false);
        visible(shell.navigation, false);
        detail = PageDetailBinding.inflate(getLayoutInflater(), shell.content, false);
        detail.detailBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
        detail.favorite.setOnClickListener(view -> {
            if (!model.canSaveLocalFavorite()) {
                Snackbar.make(detail.getRoot(), R.string.catalog_favorite_error, Snackbar.LENGTH_SHORT).show();
                return;
            }
            boolean selected = model.toggleFavorite(id);
            renderFavorite(selected);
            Snackbar.make(detail.getRoot(), selected ? R.string.catalog_saved_on_device
                    : R.string.catalog_removed_on_device, Snackbar.LENGTH_SHORT).show();
        });
        detail.retry.setOnClickListener(view -> model.openHeritage(id));
        replace(detail.getRoot(), true);
        model.openHeritage(id);
        renderDetail();
    }

    private void renderDetail() {
        if (detail == null) return;
        Heritage entry = model.detail.getValue();
        String note = model.detailNote.getValue();
        boolean ready = entry != null && entry.id == detailId;
        visible(detail.coverFrame, ready);
        visible(detail.introHeading, ready);
        visible(detail.title, ready);
        detail.favorite.setVisibility(ready ? View.VISIBLE : View.INVISIBLE);
        if (ready) {
            catalogCovers.bind(detail.cover, detail.art, entry);
            showText(detail.meta, CatalogAdapter.metaLabel(detail.getRoot(), entry));
            detail.title.setText(entry.title);
            String body = entry.description;
            detail.body.setText(CatalogSearch.normalize(body).isEmpty()
                    ? getString(R.string.catalog_detail_missing) : body);
            renderFavorite(model.isFavorite(detailId));
        } else {
            visible(detail.meta, false);
            detail.body.setText(note == null ? getString(R.string.catalog_detail_loading) : note);
        }
        visible(detail.retry, note != null);
    }

    private void renderFavorite(boolean selected) {
        var button = detail.favorite;
        button.setSelected(selected);
        button.setText(selected ? R.string.catalog_favorited : R.string.catalog_favorite);
        button.setContentDescription(getString(selected ? R.string.catalog_unfavorite : R.string.catalog_favorite));
        button.setIconResource(selected ? R.drawable.ic_catalog_bookmark_filled : R.drawable.ic_catalog_bookmark);
        int tint = MaterialColors.getColor(button, selected
                ? com.google.android.material.R.attr.colorPrimary
                : com.google.android.material.R.attr.colorOnSurface);
        button.setTextColor(tint);
        button.setIconTint(ColorStateList.valueOf(tint));
    }

    private void showExperience() {
        stage = PageStageBinding.inflate(getLayoutInflater(), shell.content, false);
        stage.stage.setOnClickListener(view -> setExperience(Experience.STAGE));
        stage.gallery.setOnClickListener(view -> setExperience(Experience.GALLERY));
        stage.create.setOnClickListener(view -> setExperience(Experience.CREATE));
        stage.light.setOnClickListener(view -> {
            lit = !lit;
            renderStage();
            stage.stageView.setAlpha(.72f);
            stage.stageView.animate().alpha(1f).setDuration(240).start();
        });
        stage.shape.setOnCheckedStateChangeListener((group, checked) -> {
            if (updatingStage || checked.isEmpty()) return;
            int id = checked.get(0);
            currentFigure().shape = id == R.id.warrior ? "warrior" : id == R.id.lady ? "lady" : "scholar";
            renderStage();
        });
        stage.palette.setOnCheckedStateChangeListener((group, checked) -> {
            if (updatingStage || checked.isEmpty()) return;
            int id = checked.get(0);
            model.draft.palette = id == R.id.indigo ? "indigo" : id == R.id.amber ? "amber" : "cinnabar";
            renderStage();
        });
        stage.moveLeft.setOnClickListener(view -> move(-.05f, 0));
        stage.moveRight.setOnClickListener(view -> move(.05f, 0));
        stage.moveUp.setOnClickListener(view -> move(0, -.05f));
        stage.moveDown.setOnClickListener(view -> move(0, .05f));
        stage.rotation.addOnChangeListener((slider, angle, fromUser) -> {
            if (!fromUser) return;
            model.draft.angle = angle;
            stage.stageView.invalidate();
        });
        stage.workName.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!updatingStage) model.draft.name = s.toString();
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
        stage.saveWork.setOnClickListener(view -> {
            hideKeyboard();
            model.saveWork();
        });
        stage.newWork.setOnClickListener(view -> {
            model.draft = new Work();
            renderStage();
        });
        renderStage();
        replace(stage.getRoot(), true);
    }

    private void setExperience(Experience next) {
        experience = next;
        renderStage();
    }

    private Work currentFigure() { return experience == Experience.CREATE ? model.draft : galleryFigure; }

    private void move(float dx, float dy) {
        model.draft.x = ShadowArtView.clamp(model.draft.x + dx, .08f, .92f);
        model.draft.y = ShadowArtView.clamp(model.draft.y + dy, .22f, .78f);
        stage.stageView.invalidate();
    }

    private void renderStage() {
        if (stage == null) return;
        boolean playing = experience == Experience.STAGE;
        boolean editing = experience == Experience.CREATE;
        stage.heading.setText(playing ? R.string.stage_title : editing ? R.string.create_title : R.string.gallery_title);
        stage.description.setText(playing ? R.string.stage_copy : editing ? R.string.create_copy : R.string.gallery_copy);
        select(stage.stage, playing);
        select(stage.gallery, experience == Experience.GALLERY);
        select(stage.create, editing);
        visible(stage.light, playing);
        visible(stage.characters, !playing);
        visible(stage.galleryInfo, experience == Experience.GALLERY);
        visible(stage.editor, editing);
        Work work = currentFigure();
        if (playing) {
            work = new Work();
            work.name = "host";
            work.x = .36f;
        }
        stage.stageView.showWork(work, !playing || lit, editing);
        updatingStage = true;
        stage.shape.check("warrior".equals(work.shape) ? R.id.warrior : "lady".equals(work.shape) ? R.id.lady : R.id.scholar);
        stage.galleryInfo.setText(switch (work.shape) {
            case "warrior" -> "武生 · 对称的盔翎与扎靠，突出动作的力度。";
            case "lady" -> "花旦 · 花冠和袖口常见镂刻纹样。";
            default -> "书生 · 方巾与长衫，轮廓舒展。";
        });
        if (editing) {
            stage.palette.check("indigo".equals(work.palette) ? R.id.indigo : "amber".equals(work.palette) ? R.id.amber : R.id.cinnabar);
            stage.rotation.setValue(Math.round(work.angle));
            if (!value(stage.workName).equals(work.name)) stage.workName.setText(work.name);
            stage.saveWork.setEnabled(!Boolean.TRUE.equals(model.busy.getValue()));
        }
        updatingStage = false;
    }

    private void select(MaterialButton button, boolean selected) {
        int fill = selected ? MaterialColors.getColor(button, com.google.android.material.R.attr.colorPrimaryContainer)
                : MaterialColors.getColor(button, com.google.android.material.R.attr.colorSurface);
        button.setBackgroundTintList(ColorStateList.valueOf(fill));
        button.setSelected(selected);
    }

    private void showProfile() {
        profile = PageProfileBinding.inflate(getLayoutInflater(), shell.content, false);
        workCards = new WorksAdapter(this::workMenu);
        profile.works.setLayoutManager(new LinearLayoutManager(this));
        profile.works.setAdapter(workCards);
        profile.edit.setOnClickListener(view -> editProfile());
        profile.retry.setOnClickListener(view -> model.loadProfile());
        profile.logout.setOnClickListener(view -> {
            registering = false;
            model.logout();
        });
        replace(profile.getRoot(), true);
        renderProfile();
        model.loadProfile();
    }

    private void renderProfile() {
        if (profile == null) return;
        Profile person = model.profile.getValue();
        profile.name.setText(person == null ? "访客" : person.nickname);
        profile.account.setText(person == null ? "" : getString(R.string.account_label, person.username));
        showText(profile.bio, person == null ? null : person.bio);
        profile.edit.setEnabled(person != null);
        List<Work> works = model.works.getValue() == null ? Collections.emptyList() : model.works.getValue();
        workCards.submitList(works);
        String note = model.profileNote.getValue();
        if (note == null && works.isEmpty()) note = getString(R.string.profile_empty);
        showText(profile.message, note);
        visible(profile.retry, model.profileNote.getValue() != null);
    }

    private void editProfile() {
        Profile current = model.profile.getValue();
        if (current == null) return;
        DialogProfileBinding form = DialogProfileBinding.inflate(getLayoutInflater());
        form.nickname.setText(current.nickname);
        form.bio.setText(current.bio);
        var dialog = new MaterialAlertDialogBuilder(this).setTitle(R.string.edit_profile)
                .setView(form.getRoot()).setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null).show();
        dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
            String nickname = value(form.nickname).trim();
            if (nickname.isEmpty()) {
                form.nicknameField.setError("请输入昵称");
                return;
            }
            model.saveProfile(nickname, value(form.bio).trim());
            dialog.dismiss();
        });
    }

    private void workMenu(Work work) {
        String[] choices = {getString(R.string.edit_work), getString(R.string.remove)};
        new MaterialAlertDialogBuilder(this).setTitle(work.name).setItems(choices, (dialog, index) -> {
            if (index == 0) {
                model.draft = work.copy();
                shell.navigation.setSelectedItemId(R.id.tab_stage);
                setExperience(Experience.CREATE);
            } else {
                new MaterialAlertDialogBuilder(this).setTitle("删除这件皮影作品？")
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.remove, (confirm, option) -> model.deleteWork(work)).show();
            }
        }).show();
    }

    private void clearPage() {
        rememberCatalogPosition();
        auth = null;
        discover = null;
        stage = null;
        profile = null;
        detail = null;
    }

    private void rememberCatalogPosition() {
        if (discover != null && discover.items.getLayoutManager() != null) {
            catalogPosition = discover.items.getLayoutManager().onSaveInstanceState();
        }
    }

    private void replace(View view, boolean animate) {
        shell.content.removeAllViews();
        shell.content.addView(view);
        if (animate) {
            view.setAlpha(0f);
            view.animate().alpha(1f).setDuration(160).start();
        }
    }

    private void hideKeyboard() {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(shell.content.getWindowToken(), 0);
    }

    private void visible(View view, boolean show) { view.setVisibility(show ? View.VISIBLE : View.GONE); }

    private void showText(TextView view, String text) {
        view.setText(text);
        visible(view, text != null && !text.isEmpty());
    }

    private String value(TextView field) { return field.getText() == null ? "" : field.getText().toString(); }

    @Override protected void onSaveInstanceState(Bundle out) {
        rememberCatalogPosition();
        out.putInt("tab", tab);
        out.putString("catalogQuery", catalogQuery);
        out.putParcelable("catalogPosition", catalogPosition);
        out.putLong("detail", detailId);
        out.putBoolean("register", registering);
        out.putBoolean("lit", lit);
        out.putString("experience", experience.name());
        super.onSaveInstanceState(out);
    }
}
