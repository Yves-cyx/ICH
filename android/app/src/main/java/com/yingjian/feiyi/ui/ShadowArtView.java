package com.yingjian.feiyi.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.yingjian.feiyi.data.Models.Work;

public final class ShadowArtView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path pen = new Path();
    private String artwork = "shadow";
    private Work figure = new Work();
    private boolean lit = true;
    private boolean editable;

    public ShadowArtView(Context context, AttributeSet attrs) { super(context, attrs); }

    void setArtwork(String artwork) {
        this.artwork = artwork == null ? "shadow" : artwork;
        invalidate();
    }

    void showWork(Work work, boolean lit, boolean editable) {
        artwork = "shadow";
        figure = work;
        this.lit = lit;
        this.editable = editable;
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() == 0 || getHeight() == 0) return;
        canvas.save();
        canvas.scale(getWidth() / 320f, getHeight() / 240f);
        switch (artwork) {
            case "brocade": pattern(canvas); break;
            case "paper": paper(canvas); break;
            case "opera": opera(canvas); break;
            case "porcelain": porcelain(canvas); break;
            case "guqin": guqin(canvas); break;
            default: stage(canvas);
        }
        canvas.restore();
    }

    private void stage(Canvas c) {
        background(c, "#442019", "#1D1111");
        paint.setShader(new RadialGradient(160, 116, 135,
                lit ? color("#FAD184") : color("#A66D3D"),
                lit ? color("#925133") : color("#4D3228"), Shader.TileMode.CLAMP));
        c.drawRoundRect(17, 23, 303, 211, 13, 13, paint);
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f);
        paint.setColor(color("#DAA56C"));
        c.drawRoundRect(17, 23, 303, 211, 13, 13, paint);
        c.drawRoundRect(23, 29, 297, 205, 9, 9, paint);
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 8; i++) {
            int x = 23 + i * 39;
            polygon(c, "#632F25", x, 19, x + 40, 19, x + 26, 41, x + 11, 41);
        }
        for (int i = 0; i < 6; i++) {
            float height = 188 + (i % 2) * 6;
            line(c, "#C28A59", 1, 35, height, 285, height);
        }
        if ("host".equals(figure.name)) {
            c.save();
            c.translate(216, 135);
            c.scale(-.76f, .76f);
            puppet(c, "lady", "indigo");
            c.restore();
        }
        c.save();
        c.translate(32 + figure.x * 256, 25 + figure.y * 174);
        c.rotate(figure.angle);
        puppet(c, figure.shape, figure.palette);
        c.restore();
        rectangle(c, "#4E221A", 0, 210, 320, 240);
        line(c, "#D49862", 2, 14, 219, 306, 219);
        line(c, "#9D5A3D", 1, 28, 227, 292, 227);
    }

    private void puppet(Canvas c, String shape, String palette) {
        final String edge = lit ? "#30201C" : "#3A261E";
        final String primary;
        if (!lit) primary = "#76513A";
        else if ("indigo".equals(palette)) primary = "#336366";
        else if ("amber".equals(palette)) primary = "#BD752F";
        else primary = "#AD392B";
        final boolean lady = "lady".equals(shape);
        final boolean warrior = "warrior".equals(shape);
        String metal = lit ? "#EAC58A" : "#9D744F";

        line(c, edge, 2, -78, 76, 43, -14);
        line(c, edge, 1.5f, 81, 74, 68, 8);
        polygon(c, edge, -18, 34, 24, 33, 31, 76, 3, 79, -2, 55, -13, 75, -36, 78);
        polygon(c, primary, -18, -23, 13, -24, 26, -3, 30, 33, 38, 56, 2, 50, -29, 58, -24, 22);
        polygon(c, edge, -24, -16, -12, -10, -27, 14, -49, 26, -53, 18, -37, 3);
        polygon(c, primary, 18, -20, 31, -13, 44, -17, 60, -24, 67, -17, 51, -5, 29, 5, 21, 0);
        polygon(c, metal, 61, -25, 73, -24, 76, -21, 67, -15, 62, -16);
        polygon(c, edge, -16, -54, -1, -63, 16, -56, 20, -44, 31, -41, 21, -35, 22, -30, 10, -26, 7, -20, -11, -24);
        polygon(c, metal, -9, -49, 5, -55, 14, -49, 16, -42, 23, -39, 16, -36, 17, -32, 9, -30, 6, -23, -6, -26);
        if (warrior) {
            polygon(c, primary, -22, -60, 0, -82, 21, -70, 16, -57);
            line(c, edge, 2, 5, -76, -24, -108);
            line(c, edge, 2, 8, -78, 23, -106);
        } else if (lady) {
            polygon(c, primary, -25, -50, -28, -78, -14, -73, -8, -90, 1, -74, 15, -78, 22, -60);
            for (int i = 0; i < 4; i++) dot(c, metal, -19 + i * 10, -69, 2.4f);
        } else {
            polygon(c, primary, -23, -54, -18, -76, 9, -78, 20, -62, 20, -54);
            line(c, metal, 2, -25, -58, 29, -59);
        }
        dot(c, edge, 12, -45, 1.8f);
        dot(c, metal, 24, -13, 3.3f);
        dot(c, metal, 47, -13, 2.6f);
        dot(c, metal, -32, 7, 2.9f);
        for (int i = 0; i < 5; i++) {
            float y = 4 + i * 9;
            line(c, metal, 1.2f, -17, y + 3, 18 + i * 2, y);
            dot(c, metal, (i % 2) * 8 - 1, y + 4, 2.2f);
        }
    }

    private void pattern(Canvas c) {
        background(c, "#39282B", "#75412E");
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 7; column++) {
                float x = column * 56 + (row % 2) * 27;
                float y = row * 57;
                polygon(c, "#C27B4E", x, y - 24, x + 25, y, x, y + 24, x - 25, y);
                polygon(c, "#513A3D", x, y - 16, x + 17, y, x, y + 16, x - 17, y);
                dot(c, "#E6B877", x, y, 5);
            }
        }
    }

    private void paper(Canvas c) {
        background(c, "#F3C491", "#D58F64");
        c.save();
        c.translate(160, 120);
        for (int i = 0; i < 8; i++) {
            c.rotate(45);
            polygon(c, "#A53A31", 0, -11, -27, -39, -17, -76, 0, -92, 17, -76, 27, -39);
            polygon(c, "#EAA879", 0, -37, -8, -53, 0, -68, 8, -53);
        }
        dot(c, "#832E27", 0, 0, 25);
        dot(c, "#EEBE86", 0, 0, 11);
        c.restore();
    }

    private void opera(Canvas c) {
        background(c, "#572628", "#251921");
        for (int i = 0; i <= 12; i++) line(c, "#773739", 3, i * 29, 0, i * 27 - 19, 240);
        c.save();
        c.translate(161, 122);
        c.scale(1.35f, 1.35f);
        puppet(c, "lady", "amber");
        c.restore();
    }

    private void porcelain(Canvas c) {
        background(c, "#D6DED4", "#86A4A5");
        c.save();
        c.translate(160, 55);
        polygon(c, "#EEF0DD", -35, 0, 35, 0, 29, 27, 46, 48, 64, 75, 65, 113, 43, 147, -43, 147, -65, 113, -64, 75, -46, 48, -29, 27);
        dot(c, "#315C78", 0, 93, 27);
        dot(c, "#EEF0DD", 0, 93, 22);
        for (int i = 0; i < 6; i++) {
            float x = (float) Math.sin(i * Math.PI / 3) * 13;
            float y = (float) Math.cos(i * Math.PI / 3) * 13;
            dot(c, "#315C78", x, 93 + y, 4);
        }
        line(c, "#315C78", 3, -37, 5, 37, 5);
        line(c, "#315C78", 3, -44, 138, 44, 138);
        c.restore();
    }

    private void guqin(Canvas c) {
        background(c, "#D8B686", "#826749");
        c.save();
        c.rotate(-22, 160, 118);
        paint.setColor(color("#4A2A22"));
        c.drawRoundRect(24, 85, 296, 151, 33, 33, paint);
        for (int i = 0; i < 7; i++) line(c, "#E3C58E", 1.1f, 44, 100 + i * 6, 274, 100 + i * 6);
        for (int i = 0; i < 9; i++) dot(c, "#ECDAAC", 57 + i * 25, 141, 1.7f);
        c.restore();
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (!editable) return super.onTouchEvent(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                getParent().requestDisallowInterceptTouchEvent(true);
                figure.x = clamp((event.getX() / getWidth() * 320 - 32) / 256, .08f, .92f);
                figure.y = clamp((event.getY() / getHeight() * 240 - 25) / 174, .22f, .78f);
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                if (event.getActionMasked() == MotionEvent.ACTION_UP) performClick();
                return true;
            default: return false;
        }
    }

    @Override public boolean performClick() { return super.performClick(); }

    static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private void background(Canvas c, String first, String second) {
        paint.setShader(new LinearGradient(0, 0, 320, 240, color(first), color(second), Shader.TileMode.CLAMP));
        c.drawRect(0, 0, 320, 240, paint);
        paint.setShader(null);
    }

    private void rectangle(Canvas c, String tint, float left, float top, float right, float bottom) {
        paint.setColor(color(tint));
        c.drawRect(left, top, right, bottom, paint);
    }

    private void dot(Canvas c, String tint, float x, float y, float radius) {
        paint.setColor(color(tint));
        c.drawCircle(x, y, radius, paint);
    }

    private void line(Canvas c, String tint, float weight, float x, float y, float x2, float y2) {
        paint.setColor(color(tint));
        paint.setStrokeWidth(weight);
        c.drawLine(x, y, x2, y2, paint);
    }

    private void polygon(Canvas c, String tint, float... points) {
        paint.setColor(color(tint));
        pen.reset();
        pen.moveTo(points[0], points[1]);
        for (int i = 2; i < points.length; i += 2) pen.lineTo(points[i], points[i + 1]);
        pen.close();
        c.drawPath(pen, paint);
    }

    private int color(String hex) { return Color.parseColor(hex); }
}
