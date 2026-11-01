package com.lightfit.app.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/** 近 7 天训练分钟数柱状图（自绘，不引第三方图表库，符合 BP 轻量原则） */
public class BarView extends View {

    private int[] data = new int[7];
    private final Paint bar = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);

    public BarView(Context c) {
        super(c);
        init();
    }

    public BarView(Context c, AttributeSet a) {
        super(c, a);
        init();
    }

    public BarView(Context c, AttributeSet a, int s) {
        super(c, a, s);
        init();
    }

    private void init() {
        bar.setColor(Color.rgb(22, 50, 79));
        label.setColor(Color.DKGRAY);
        label.setTextSize(26f);
        label.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(int[] d) {
        if (d != null && d.length == 7) data = d.clone();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        int max = 1;
        for (int v : data) max = Math.max(max, v);
        float bw = w / 7f;
        for (int i = 0; i < 7; i++) {
            if (data[i] > 0) {
                float bh = (h - 44f) * data[i] / max;
                canvas.drawRoundRect(bw * i + bw * 0.18f, h - 34f - bh,
                        bw * (i + 1) - bw * 0.18f, h - 34f, 10f, 10f, bar);
            }
            canvas.drawText(String.valueOf(i + 1), bw * i + bw / 2f, h - 10f, label);
        }
    }
}
