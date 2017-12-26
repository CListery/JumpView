package com.cyh.jumpview;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by yh on 17-12-14.
 */

public class JumpView extends View {

    private final String LOG_TAG = "JumpView";

    private Paint mPaint;

    private static final int DEFAULT_POINT_COUNT = 3;
    private static final int DEFAULT_LOOP_DURATION = 1300;
    private static final float DEFAULT_ANIMATION_DUTY_CYCLE = 0.65f;
    private static final float DEFAULT_JUMP_RANGE = 0.52f;
    private static final float DEFAULT_RADIUS = 15;
    private static final float DEFAULT_Y_OFFSET = 0.8f;

    private RectF[] mPointRect;
    private int[] mShift;
    private ValueAnimator[] mJumpAnimators;
    float[] points = new float[DEFAULT_POINT_COUNT * 2];

    private int mPointCount = DEFAULT_POINT_COUNT;
    private int mAnimDuration = DEFAULT_LOOP_DURATION;
    private float mAnimRange = DEFAULT_ANIMATION_DUTY_CYCLE;
    private float mJumpRange = DEFAULT_JUMP_RANGE;
    private float mRadius = DEFAULT_RADIUS;
    private float mOffsetY = DEFAULT_Y_OFFSET;

    private AtomicBoolean mCanRunning = new AtomicBoolean(Boolean.FALSE);

    private int mWidth;
    private int mHeight;
    private float mCoordinateX;
    private float mCoordinateY;

    public JumpView(Context context) {
        super(context);
        Log.d(LOG_TAG, "JumpView 1");
        init();
    }

    public JumpView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Log.d(LOG_TAG, "JumpView 2");
        init();
    }

    public JumpView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.d(LOG_TAG, "JumpView 3");
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public JumpView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Log.d(LOG_TAG, "JumpView 4");
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(3);
        mPaint.setColor(Color.parseColor("#FF33B5E5"));
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        int pointDelay = mAnimDuration / (3 * mPointCount);
        JumpInterpolator interpolator = new JumpInterpolator(mAnimRange);
        mShift = new int[mPointCount];
        mPointRect = new RectF[mPointCount];
        mJumpAnimators = new ValueAnimator[mPointCount];
        for (int index = 0; index < mPointCount; index++) {
            mPointRect[index] = new RectF();
            mJumpAnimators[index] = new ValueAnimator();
            mJumpAnimators[index].setDuration(mAnimDuration).setStartDelay(index * pointDelay);
            mJumpAnimators[index].setInterpolator(interpolator);
            mJumpAnimators[index].setRepeatCount(ValueAnimator.INFINITE);
            mJumpAnimators[index].setRepeatMode(ValueAnimator.RESTART);
            mJumpAnimators[index].addUpdateListener(new JumpAnimUpdateListener(index));
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int paddingL = getPaddingLeft();
        int paddingT = getPaddingTop();
        int paddingR = getPaddingRight();
        int paddingB = getPaddingBottom();
        Log.d(LOG_TAG, "onLayout: " + changed + " - " + left + " - " + top + " - " + right + " - " + bottom);
        Log.d(LOG_TAG, "onLayout: " + paddingL + " - " + paddingT + " - " + paddingR + " - " + paddingB);

        mWidth = right - left - getPaddingRight() - getPaddingLeft();
        mHeight = bottom - top - getPaddingBottom() - getPaddingTop();

        mCoordinateX = mWidth / (mPointCount * 2f);
        mCoordinateY = mHeight * mOffsetY - mRadius;

        int ascent = top - bottom - getPaddingTop() - getPaddingBottom();
        int maxShift = (int) (ascent / 2 * mJumpRange);
        Log.d(LOG_TAG, "onLayout: " + ascent + " - " + maxShift + " - " + mCanRunning);

        for (ValueAnimator animator : mJumpAnimators) {
            if (animator.isRunning()) {
                animator.cancel();
            }
            animator.setIntValues(0, maxShift);
            if (mCanRunning.get()) {
                animator.start();
            }
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int index = 0; index < points.length; index += 2) {
            points[index] = mCoordinateX * (1 + index);
            points[index + 1] = mCoordinateY;
            int pointIndex = index / 2;
            RectF currentR = mPointRect[pointIndex];
            float shift = mShift[pointIndex];
            currentR.top = points[index + 1] - mRadius + shift;
            currentR.bottom = points[index + 1] + mRadius + shift;
            currentR.left = points[index] - mRadius;
            currentR.right = points[index] + mRadius;
            canvas.drawArc(currentR, 0, 360, true, mPaint);
        }
    }

    public void resume() {
        Log.d(LOG_TAG, "resume: " + mCanRunning);
        mCanRunning.set(true);
    }

    public void stop() {
        Log.d(LOG_TAG, "stop: " + mCanRunning);
        if (mCanRunning.compareAndSet(true, false)) {
            for (ValueAnimator animator : mJumpAnimators) {
                if (null != animator) {
                    animator.removeAllUpdateListeners();
                    if (animator.isRunning()) {
                        if (null != animator.getValues()) {
                            animator.cancel();
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        resume();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stop();
    }

    private class JumpAnimUpdateListener implements ValueAnimator.AnimatorUpdateListener {

        private int mIndex;

        public JumpAnimUpdateListener(int index) {
            mIndex = index;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            if (mCanRunning.get()) {
                mShift[mIndex] = (int) animation.getAnimatedValue();
                invalidate();
            }
        }
    }

    private class JumpInterpolator implements TimeInterpolator {

        private final float animRange;

        public JumpInterpolator(float animatedRange) {
            animRange = Math.abs(animatedRange);
        }

        @Override
        public float getInterpolation(float input) {
            if (input > animRange) {
                return 0f;
            }
            double radians = (input / animRange) * Math.PI;
            return (float) Math.sin(radians);
        }

    }
}
