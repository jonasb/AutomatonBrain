package se.forskningsavd.automatonbrain;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import static android.view.MotionEvent.*;

class NavigationView extends View {
    private static final float DEADZONE = 0.1f; // 0..1
    private final Paint mPaint = new Paint();
    private final Navigator mNavigator;
    private int mMoveCenterX;
    private int mMoveCenterY;
    private int mMoveDistanceX;
    private int mMoveDistanceY;
    private int mLookCenterX;
    private int mLookCenterY;
    private int mLookDistanceX;
    private int mLookDistanceY;

    public NavigationView(Context context, Navigator navigator) {
        super(context);
        mNavigator = navigator;
        mPaint.setColor(0xffffffff);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mMoveCenterX = w / 4;
        mLookCenterX = w * 3 / 4;
        mMoveCenterY = mLookCenterY = h / 2;

        mMoveDistanceX = mLookDistanceX = w / 4;
        mMoveDistanceY = mLookDistanceY = h / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawRings(canvas, mMoveCenterX, mMoveCenterY, mMoveDistanceX, mMoveDistanceY, mPaint);
        drawRings(canvas, mLookCenterX, mLookCenterY, mLookDistanceX, mLookDistanceY, mPaint);
    }

    private void drawRings(Canvas canvas, int centerX, int centerY, int radiusX, int radiusY, Paint paint) {
        final int minRadius = Math.min(radiusX, radiusY);
        final int rings = minRadius / 30;
        final float ringDistance = (float) minRadius / rings;
        for (int i = 0; i < rings; i++) {
            canvas.drawCircle(centerX, centerY, (i + 1) * ringDistance, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
        case ACTION_POINTER_DOWN:
            return true;
        case ACTION_MOVE:
        case ACTION_DOWN:
            for (int i = 0; i < event.getPointerCount(); i++) {
                final float x = event.getX(i);
                final float y = event.getY(i);
                final float moveX = normalizeAxis(x, mMoveCenterX, mMoveDistanceX);
                final float moveY = normalizeAxis(y, mMoveCenterY, mMoveDistanceY);
                final float lookX = normalizeAxis(x, mLookCenterX, mLookDistanceX);
                final float lookY = normalizeAxis(y, mLookCenterY, mLookDistanceY);

                if (Math.abs(moveX) <= 1.f && Math.abs(moveY) <= 1.f) {
                    mNavigator.moveX = moveX;
                    mNavigator.moveY = moveY;
                } else if (Math.abs(lookX) <= 1.f && Math.abs(lookY) <= 1.f) {
                    mNavigator.rotation = lookX;
                    mNavigator.cameraAngle = lookY;
                }
            }
            /*
            // wasd
            mNavigator.left = (x < width / 3);
            mNavigator.right = (x > width * 2 / 3);
            mNavigator.up = (y < height / 3);
            mNavigator.down = (y > height * 2 / 3);
            */
            return true;
        case ACTION_CANCEL:
        case ACTION_UP:
            mNavigator.reset();
            return true;
        default:
            return false;
        }
    }

    private float normalizeAxis(float actual, int reference, int maxDistance) {
        float d = actual - reference;
        final float dead = DEADZONE * maxDistance;
        if (Math.abs(d) < dead) {
            return 0;
        }
        d += (d > 0 ? dead : -dead);
        return (d / maxDistance);
    }
}
