package se.forskningsavd;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import static android.view.MotionEvent.*;

class NavigationView extends View {
    private final Paint mPaint = new Paint();
    private final Navigator mNavigator;

    public NavigationView(Context context, Navigator navigator) {
        super(context);
        mNavigator = navigator;
        mPaint.setColor(0xffffffff);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int width = getWidth();
        final int height = getHeight();
        canvas.drawLine(0, height / 3, width, height / 3, mPaint);
        canvas.drawLine(0, height * 2 / 3, width, height * 2 / 3, mPaint);
        canvas.drawLine(width / 3, 0, width / 3, height, mPaint);
        canvas.drawLine(width * 2 / 3, 0, width * 2 / 3, height, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
        case ACTION_MOVE:
        case ACTION_DOWN:
            final int width = getWidth();
            final int height = getHeight();

            final float x = event.getX();
            final float y = event.getY();
            mNavigator.left = (x < width / 3);
            mNavigator.right = (x > width * 2 / 3);
            mNavigator.up = (y < height / 3);
            mNavigator.down = (y > height * 2 / 3);
            return true;
        case ACTION_CANCEL:
        case ACTION_UP:
            mNavigator.up = mNavigator.down = mNavigator.left = mNavigator.right = false;
            return true;
        default:
            return false;
        }
    }
}
