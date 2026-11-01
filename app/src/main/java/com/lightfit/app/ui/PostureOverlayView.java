package com.lightfit.app.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

/** 姿态骨架叠加层（前摄预览未镜像，骨架与预览存在水平翻转，v1 可接受） */
public class PostureOverlayView extends View {

    private static final int[][] BONES = {
            {PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER},
            {PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW},
            {PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST},
            {PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW},
            {PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST},
            {PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP},
            {PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP},
            {PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP},
    };

    private static final int[] JOINTS = {
            PoseLandmark.NOSE,
            PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER,
            PoseLandmark.LEFT_ELBOW, PoseLandmark.RIGHT_ELBOW,
            PoseLandmark.LEFT_WRIST, PoseLandmark.RIGHT_WRIST,
            PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP,
    };

    private Pose pose;
    private int imgW, imgH;
    private final Paint joint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bone = new Paint(Paint.ANTI_ALIAS_FLAG);

    public PostureOverlayView(Context c) {
        super(c);
        init();
    }

    public PostureOverlayView(Context c, AttributeSet a) {
        super(c, a);
        init();
    }

    public PostureOverlayView(Context c, AttributeSet a, int s) {
        super(c, a, s);
        init();
    }

    private void init() {
        joint.setColor(Color.argb(235, 62, 207, 168));
        joint.setStrokeWidth(10f);
        bone.setColor(Color.argb(190, 255, 255, 255));
        bone.setStrokeWidth(4f);
        bone.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setPose(Pose p, int w, int h) {
        pose = p;
        imgW = w;
        imgH = h;
        postInvalidateOnAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (pose == null || imgW <= 0 || imgH <= 0) return;
        float sx = getWidth() / (float) imgW;
        float sy = getHeight() / (float) imgH;
        for (int[] b : BONES) {
            PoseLandmark a = pose.getPoseLandmark(b[0]);
            PoseLandmark z = pose.getPoseLandmark(b[1]);
            if (a == null || z == null) continue;
            canvas.drawLine(a.getPosition().x * sx, a.getPosition().y * sy,
                    z.getPosition().x * sx, z.getPosition().y * sy, bone);
        }
        for (int id : JOINTS) {
            PoseLandmark l = pose.getPoseLandmark(id);
            if (l == null) continue;
            canvas.drawPoint(l.getPosition().x * sx, l.getPosition().y * sy, joint);
        }
    }
}
