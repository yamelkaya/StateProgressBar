package com.kofigyan.stateprogressbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Scroller;

import com.kofigyan.stateprogressbar.utils.FontManager;

import java.util.ArrayList;


/**
 * Created by Kofi Gyan on 4/19/2016.
 */

public class StateProgressBar extends View {


    public static final float EMPTY_CIRCLE_SCALE_RATE = 1.5f;
    private State[] states;
    private String unitLabel;

    public enum StateNumber {
        ZERO(0), ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5);
        private int value;

        private StateNumber(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private static final int MIN_STATE_NUMBER = 1;
    private static final int MAX_STATE_NUMBER = 5;

    private static final String END_CENTER_X_KEY = "mEndCenterX";
    private static final String START_CENTER_X_KEY = "mStartCenterX";
    private static final String ANIM_START_X_POS_KEY = "mAnimStartXPos";
    private static final String ANIM_END_X_POS_KEY = "mAnimEndXPos";
    private static final String IS_CURRENT_ANIM_STARTED_KEY = "mIsCurrentAnimStarted";
    private static final String ANIMATE_TO_CURRENT_PROGRESS_STATE_KEY = "mAnimateToCurrentProgressState";
    private static final String SUPER_STATE_KEY = "superState";

    private ArrayList<String> mStateDescriptionData = new ArrayList<String>();

    private float mStateRadius;
    private float mStateSize;
    private float mStateLineThickness;
    private float mStateNumberTextSize;
    private float mStateDescriptionSize;

    /**
     * width of one cell = stageWidth/noOfStates
     */
    private float mCellWidth;

    private float mCellHeight;

    private float mPaddingHorizontal;

    /**
     * next cell(state) from previous cell
     */
    private float mNextCellWidth;

    /**
     * center of first cell(state)
     */
    private float mStartCenterX;

    /**
     * center of last cell(state)
     */
    private float mEndCenterX;

    private int mMaxStateNumber;
    private int mCurrentStateNumber;

    private int mAnimStartDelay;
    private int mAnimDuration;

    private float mSpacing;

    private float mDescTopSpaceDecrementer;
    private float mDescTopSpaceIncrementer;

    private static final float DEFAULT_TEXT_SIZE = 15f;
    private static final float DEFAULT_STATE_SIZE = 25f;

    /**
     * Paints for drawing
     */
    private Paint mStateNumberForegroundPaint;
    private Paint mStateCheckedForegroundPaint;
    private Paint mStateNumberBackgroundPaint;
    private Paint mBackgroundPaint;
    private Paint mForegroundPaint;
    private Paint mCurrentStateDescriptionPaint;
    private Paint mStateDescriptionPaint;

    private int mBackgroundColor;
    private int mForegroundColor;
    private int mStateNumberBackgroundColor;
    private int mStateNumberForegroundColor;
    private int mCurrentStateDescriptionColor;
    private int mStateDescriptionColor;

    /**
     * animate inner line to current progress state
     */
    private Animator mAnimator;

    /**
     * tracks progress of line animator
     */
    private float mAnimStartXPos;
    private float mAnimEndXPos;

    private boolean mIsCurrentAnimStarted;

    private boolean mAnimateToCurrentProgressState;
    private boolean mEnableAllStatesCompleted;
    private boolean mCheckStateCompleted;

    private boolean mIsStateSizeSet;
    private boolean mIsStateTextSizeSet;

    private Typeface mCheckFont;

    public StateProgressBar(Context context) {
        this(context, null, 0);
    }

    public StateProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StateProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
        initializePainters();
        updateCheckAllStatesValues(mEnableAllStatesCompleted);

    }

    private void init(Context context, AttributeSet attrs, int defStyle) {

        /**
         * Setting default values.
         */
        initStateProgressBar(context);

        mStateDescriptionSize = convertSpToPixel(mStateDescriptionSize);
        mStateLineThickness = convertDpToPixel(mStateLineThickness);
        mSpacing = convertDpToPixel(mSpacing);
        mCheckFont = FontManager.getTypeface(context, FontManager.FONTAWESOME);


        if (attrs != null) {

            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StateProgressBar, defStyle, 0);

            mBackgroundColor = a.getColor(R.styleable.StateProgressBar_spb_stateBackgroundColor, mBackgroundColor);
            mForegroundColor = a.getColor(R.styleable.StateProgressBar_spb_stateForegroundColor, mForegroundColor);
            mStateNumberBackgroundColor = a.getColor(R.styleable.StateProgressBar_spb_stateNumberBackgroundColor, mStateNumberBackgroundColor);
            mStateNumberForegroundColor = a.getColor(R.styleable.StateProgressBar_spb_stateNumberForegroundColor, mStateNumberForegroundColor);
            mCurrentStateDescriptionColor = a.getColor(R.styleable.StateProgressBar_spb_currentStateDescriptionColor, mCurrentStateDescriptionColor);
            mStateDescriptionColor = a.getColor(R.styleable.StateProgressBar_spb_stateDescriptionColor, mStateDescriptionColor);

            mCurrentStateNumber = a.getInteger(R.styleable.StateProgressBar_spb_currentStateNumber, mCurrentStateNumber);
            mMaxStateNumber = a.getInteger(R.styleable.StateProgressBar_spb_maxStateNumber, mMaxStateNumber);

            mStateSize = a.getDimension(R.styleable.StateProgressBar_spb_stateSize, mStateSize);
            mStateNumberTextSize = a.getDimension(R.styleable.StateProgressBar_spb_stateTextSize, mStateNumberTextSize);
            mStateDescriptionSize = a.getDimension(R.styleable.StateProgressBar_spb_stateDescriptionSize, mStateDescriptionSize);
            mStateLineThickness = a.getDimension(R.styleable.StateProgressBar_spb_stateLineThickness, mStateLineThickness);

            mCheckStateCompleted = a.getBoolean(R.styleable.StateProgressBar_spb_checkStateCompleted, mCheckStateCompleted);
            mAnimateToCurrentProgressState = a.getBoolean(R.styleable.StateProgressBar_spb_animateToCurrentProgressState, mAnimateToCurrentProgressState);
            mEnableAllStatesCompleted = a.getBoolean(R.styleable.StateProgressBar_spb_enableAllStatesCompleted, mEnableAllStatesCompleted);

            mDescTopSpaceDecrementer = a.getDimension(R.styleable.StateProgressBar_spb_descriptionTopSpaceDecrementer, mDescTopSpaceDecrementer);
            mDescTopSpaceIncrementer = a.getDimension(R.styleable.StateProgressBar_spb_descriptionTopSpaceIncrementer, mDescTopSpaceIncrementer);

            mAnimDuration = a.getInteger(R.styleable.StateProgressBar_spb_animationDuration, mAnimDuration);
            mAnimStartDelay = a.getInteger(R.styleable.StateProgressBar_spb_animationStartDelay, mAnimStartDelay);


            if (!mAnimateToCurrentProgressState) {
                stopAnimation();
            }

            resolveStateSize();
            validateLineThickness(mStateLineThickness);
            validateStateNumber(mCurrentStateNumber);

            mStateRadius = mStateSize / 2;

            a.recycle();

        }

    }

    private void initializePainters() {

        Typeface typefaceNormal = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);

        mBackgroundPaint = setPaintAttributes(mStateLineThickness, mBackgroundColor, Paint.Style.STROKE);
        mForegroundPaint = setPaintAttributes(mStateLineThickness, mForegroundColor, Paint.Style.FILL_AND_STROKE);
        mStateNumberForegroundPaint = setPaintAttributes(mStateNumberTextSize, mStateNumberForegroundColor, typefaceNormal);
        mStateCheckedForegroundPaint = setPaintAttributes(mStateNumberTextSize, mStateNumberForegroundColor, mCheckFont);
        mStateNumberBackgroundPaint = setPaintAttributes(mStateNumberTextSize, mStateNumberBackgroundColor, typefaceNormal);
        mCurrentStateDescriptionPaint = setPaintAttributes(mStateDescriptionSize, mCurrentStateDescriptionColor, typefaceNormal);
        mStateDescriptionPaint = setPaintAttributes(mStateDescriptionSize, mStateDescriptionColor, typefaceNormal);

    }

    private void validateLineThickness(float lineThickness) {
        float halvedStateSize = mStateSize / 2;

        if (lineThickness > halvedStateSize) {
            mStateLineThickness = halvedStateSize;
        }
    }

    private void validateStateSize() {
        if (mStateSize <= mStateNumberTextSize) {
            mStateSize = mStateNumberTextSize + mStateNumberTextSize / 2;
        }
    }

    public void setBackgroundColor(int backgroundColor) {
        mBackgroundColor = backgroundColor;
        mBackgroundPaint.setColor(mBackgroundColor);
        invalidate();
    }

    public void setForegroundColor(int foregroundColor) {
        mForegroundColor = foregroundColor;
        mForegroundPaint.setColor(mForegroundColor);
        invalidate();
    }

    public void setStateLineThickness(float stateLineThickness) {
        mStateLineThickness = convertDpToPixel(stateLineThickness);
        validateLineThickness(mStateLineThickness);
        mBackgroundPaint.setStrokeWidth(mStateLineThickness);
        mForegroundPaint.setStrokeWidth(mStateLineThickness);
        invalidate();
    }

    public void setStateNumberBackgroundColor(int stateNumberBackgroundColor) {
        mStateNumberBackgroundColor = stateNumberBackgroundColor;
        mStateNumberBackgroundPaint.setColor(mStateNumberBackgroundColor);
        invalidate();
    }

    public void setStateNumberForegroundColor(int stateNumberForegroundColor) {
        mStateNumberForegroundColor = stateNumberForegroundColor;
        mStateNumberForegroundPaint.setColor(mStateNumberForegroundColor);
        mStateCheckedForegroundPaint.setColor(mStateNumberForegroundColor);
        invalidate();
    }

    public void setStateDescriptionColor(int stateDescriptionColor) {
        mStateDescriptionColor = stateDescriptionColor;
        mStateDescriptionPaint.setColor(mStateDescriptionColor);
        invalidate();
    }

    public void setCurrentStateDescriptionColor(int currentStateDescriptionColor) {
        mCurrentStateDescriptionColor = currentStateDescriptionColor;
        mCurrentStateDescriptionPaint.setColor(mCurrentStateDescriptionColor);
        invalidate();
    }

    public void setCurrentStateNumber(int currentStateNumber) {
        validateStateNumber(currentStateNumber);
        mCurrentStateNumber = currentStateNumber;
        updateCheckAllStatesValues(mEnableAllStatesCompleted);
        recalculateCellParams();
        recalculateBarState();
        invalidate();
    }

    public void setMaxStateNumber(int maximumState) {
        mMaxStateNumber = maximumState;
        validateStateNumber(mCurrentStateNumber);
        updateCheckAllStatesValues(mEnableAllStatesCompleted);
        recalculateCellParams();
        recalculateBarState();
        invalidate();
    }

    public void setStateSize(float stateSize) {
        mStateSize = convertDpToPixel(stateSize);
        mIsStateSizeSet = true;
        resetStateSizeValues();
    }

    private void resetStateSizeValues() {

        resolveStateSize(mIsStateSizeSet, mIsStateTextSizeSet);

        mStateNumberForegroundPaint.setTextSize(mStateNumberTextSize);
        mStateNumberBackgroundPaint.setTextSize(mStateNumberTextSize);
        mStateCheckedForegroundPaint.setTextSize(mStateNumberTextSize);

        mStateRadius = mStateSize / 2;

        validateLineThickness(mStateLineThickness);

        mBackgroundPaint.setStrokeWidth(mStateLineThickness);
        mForegroundPaint.setStrokeWidth(mStateLineThickness);
        requestLayout();
    }

    public void setStateDescriptionSize(float stateDescriptionSize) {
        mStateDescriptionSize = convertSpToPixel(stateDescriptionSize);
        mCurrentStateDescriptionPaint.setTextSize(mStateDescriptionSize);
        mStateDescriptionPaint.setTextSize(mStateDescriptionSize);
        requestLayout();
    }

    private void updateCheckAllStatesValues(boolean enableAllStatesCompleted) {
        if (enableAllStatesCompleted) {
            mCheckStateCompleted = true;
            mCurrentStateNumber = mMaxStateNumber;
            mStateDescriptionPaint.setColor(mCurrentStateDescriptionPaint.getColor());
        } else {
            mStateDescriptionPaint.setColor(mStateDescriptionPaint.getColor());
        }
    }


    public void enableAnimationToCurrentState(boolean animateToCurrentProgressState) {
        this.mAnimateToCurrentProgressState = animateToCurrentProgressState;

        if (mAnimateToCurrentProgressState && mAnimator == null) {
            startAnimator();
        }

        invalidate();
    }

    private void validateStateNumber(int stateNumber) {
        if (stateNumber > mMaxStateNumber) {
            throw new IllegalStateException("State number (" + stateNumber + ") cannot be greater than total number of states " + mMaxStateNumber);
        }
    }


    public void setDescriptionTopSpaceIncrementer(float spaceIncrementer) {
        mDescTopSpaceIncrementer = spaceIncrementer;
        requestLayout();
    }

    public void setUnit(String unitLabel) {
        this.unitLabel = unitLabel;
    }


    private Paint setPaintAttributes(float strokeWidth, int color, Paint.Style style) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(style);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(color);
        return paint;
    }

    private Paint setPaintAttributes(float textSize, int color, Typeface typeface) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(textSize);
        paint.setColor(color);
        paint.setTypeface(typeface);
        return paint;
    }

    private void initStateProgressBar(Context context) {

        mBackgroundColor = ContextCompat.getColor(context, R.color.background_color);
        mForegroundColor = ContextCompat.getColor(context, R.color.foreground_color);
        mStateNumberBackgroundColor = ContextCompat.getColor(context, R.color.background_text_color);
        mStateNumberForegroundColor = ContextCompat.getColor(context, R.color.foreground_text_color);
        mCurrentStateDescriptionColor = ContextCompat.getColor(context, R.color.foreground_color);
        mStateDescriptionColor = ContextCompat.getColor(context, R.color.background_text_color);

        mStateSize = 0.0f;
        mStateLineThickness = 4.0f;
        mStateNumberTextSize = 0.0f;
        mStateDescriptionSize = 15f;

        mMaxStateNumber = StateNumber.FIVE.getValue();
        mCurrentStateNumber = StateNumber.ZERO.getValue();

        mSpacing = 4.0f;

        mDescTopSpaceDecrementer = 0.0f;
        mDescTopSpaceIncrementer = 0.0f;

        mCheckStateCompleted = false;
        mAnimateToCurrentProgressState = false;
        mEnableAllStatesCompleted = false;

        mAnimStartDelay = 100;
        mAnimDuration = 4000;

    }


    private void resolveStateSize() {
        if (mStateSize == 0 && mStateNumberTextSize == 0) {
            mIsStateSizeSet = false;
            mIsStateTextSizeSet = false;
            resolveStateSize(mIsStateSizeSet, mIsStateTextSizeSet);

        } else if (mStateSize != 0 && mStateNumberTextSize != 0) {
            mIsStateSizeSet = true;
            mIsStateTextSizeSet = true;
            resolveStateSize(mIsStateSizeSet, mIsStateTextSizeSet);

        } else if (mStateSize == 0 && mStateNumberTextSize != 0) {
            mIsStateSizeSet = false;
            mIsStateTextSizeSet = true;
            resolveStateSize(mIsStateSizeSet, mIsStateTextSizeSet);

        } else if (mStateSize != 0 && mStateNumberTextSize == 0) {
            mIsStateSizeSet = true;
            mIsStateTextSizeSet = false;
            resolveStateSize(mIsStateSizeSet, mIsStateTextSizeSet);
        }

    }

    private void resolveStateSize(boolean isStateSizeSet, boolean isStateTextSizeSet) {
        if (!isStateSizeSet && !isStateTextSizeSet) {
            mStateSize = convertDpToPixel(DEFAULT_STATE_SIZE);
            mStateNumberTextSize = convertSpToPixel(DEFAULT_TEXT_SIZE);

        } else if (isStateSizeSet && isStateTextSizeSet) {
            validateStateSize();

        } else if (!isStateSizeSet && isStateTextSizeSet) {
            mStateSize = mStateNumberTextSize + mStateNumberTextSize / 2;

        } else if (isStateSizeSet && !isStateTextSizeSet) {
            mStateNumberTextSize = mStateSize - (mStateSize * 0.375f);
        }

    }

    private void drawCircles(Canvas canvas, Paint paint, int startIndex, int endIndex, float radius) {
        for (int i = startIndex; i < endIndex; i++) {
            canvas.drawCircle(mPaddingHorizontal + mCellWidth * (i + 1) - (mCellWidth / 2), mCellHeight / 2, radius, paint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        recalculateCellParams();
    }

    private void recalculateCellParams() {
        mPaddingHorizontal = getWidth()*0f;
        mCellWidth = (getWidth() - mPaddingHorizontal*2) / getCellsNumber();
        mNextCellWidth = mCellWidth;
    }

    private int getCellsNumber() {
        return Math.min(mMaxStateNumber, StateNumber.FIVE.getValue());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawState(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height = getDesiredHeight();
        int width = MeasureSpec.getSize(widthMeasureSpec);

        setMeasuredDimension(width, height);

        mCellHeight = getCellHeight();

    }

    private int getDesiredHeight() {
        return (int) (2 * EMPTY_CIRCLE_SCALE_RATE * mStateRadius) + (int) (1.3 * mStateDescriptionSize) + (int) (mSpacing) - (int) (mDescTopSpaceDecrementer) + (int) (mDescTopSpaceIncrementer);  // mStageHeight = mCellHeight + ( 2 * description Text Size)
    }

    private int getCellHeight() {
        return (int) (2 * EMPTY_CIRCLE_SCALE_RATE * mStateRadius) + (int) (mSpacing);
    }

    private void drawState(Canvas canvas) {
        if (mMaxStateNumber > 0) {
            for (int i=0; i<states.length; i++){
                State state = states[i];
                float radius = state.filled ? mStateRadius : mStateRadius * EMPTY_CIRCLE_SCALE_RATE;
                Paint paint = state.filled ? mForegroundPaint : mBackgroundPaint;

                drawCircles(canvas, paint, i, i+1, radius);
                drawStateDescriptionText(canvas, mStateDescriptionPaint, i, state.value);

                boolean hasGapWithNext = hasGap(i,i+1);
                boolean hasGapWithPrev = hasGap(i-1,i);
                if (hasGapWithNext){
                    drawGap(canvas, mBackgroundPaint, i, mStateRadius * EMPTY_CIRCLE_SCALE_RATE);
                }
                drawLines(canvas, mForegroundPaint, i, hasGapWithPrev, hasGapWithNext, radius);
            }
        }
    }

    private boolean hasGap(int i, int j){
        return i >= 0 && j >= 0 && states.length > i && states.length > j && Math.abs(states[i].value - states[j].value) > 1;
    }

    private void recalculateBarState() {
        int cellsNumber = getCellsNumber();
        states = new State[cellsNumber];

        for (int i=0; i<states.length; i++){
            int value;
            if (i == 0){
                value = mMaxStateNumber;
            }
            else {
                if (mMaxStateNumber - mCurrentStateNumber < cellsNumber - 1){
                    value = mMaxStateNumber - i;
                }
                else if (mCurrentStateNumber < cellsNumber - 2){
                    value = cellsNumber - i;
                }
                else {
                    value = (mCurrentStateNumber + 2 - i);
                }
            }
            boolean filled = value <= mCurrentStateNumber;
            states[cellsNumber - i -1] = new State(value,filled);
        }
    }

    private void drawStartPaddingLine(Canvas canvas, Paint paint) {
        canvas.drawLine(0, mCellHeight / 2, mPaddingHorizontal, mCellHeight / 2, paint);
    }

    private void drawEndPaddingLine(Canvas canvas, Paint paint) {
        canvas.drawLine(getWidth() - mPaddingHorizontal, mCellHeight / 2, getWidth(), mCellHeight / 2, paint);
    }

    private void drawLines(Canvas canvas, Paint paint, int index, boolean leftGap, boolean rightGap, float radius) {
        float cellStart = mPaddingHorizontal + mCellWidth * index;
        float cellEnd = mPaddingHorizontal + mCellWidth * (index + 1);
        float cellCenter = cellStart + mCellWidth / 2;
        float circleStart = cellCenter - radius;
        float circleEnd = cellCenter + radius;

        if (!leftGap) {
            canvas.drawLine(cellStart, mCellHeight / 2, circleStart - 1, mCellHeight / 2, paint);
        }
        if (!rightGap) {
            canvas.drawLine(circleEnd + 1, mCellHeight / 2, cellEnd, mCellHeight / 2, paint);
        }
    }

    private void drawGap(Canvas canvas, Paint paint, Integer gapAfter, float radius) {
        if (gapAfter != null){
            float gapWidth = mCellWidth - radius*2;
            for (int j=0; j<3; j++){
                canvas.drawCircle(mPaddingHorizontal + mCellWidth * (gapAfter+1) + gapWidth/4 *(j-1), mCellHeight/2, 2, paint);
            }
        }
    }

    private void drawStateDescriptionText(Canvas canvas, Paint paint, int i, int value) {
        int xPos;
        int yPos;
        xPos = (int) (mPaddingHorizontal + mCellWidth*(i+1) - (mCellWidth / 2));
        yPos = (int) (mCellHeight + mStateDescriptionSize - mSpacing - mDescTopSpaceDecrementer + mDescTopSpaceIncrementer);//mSpacing = mStateNumberForegroundPaint.getTextSize()

        canvas.drawText(String.valueOf(value) + (i == states.length - 1 ? (" " + unitLabel) : ""), xPos, yPos, paint);
    }

    private void startAnimator() {
        mAnimator = new Animator();
        mAnimator.start();
    }

    private void stopAnimation() {
        if (mAnimator != null) {
            mAnimator.stop();
        }
    }


    private class Animator implements Runnable {
        private Scroller mScroller;
        private boolean mRestartAnimation = false;

        public Animator() {
            mScroller = new Scroller(getContext(), new AccelerateDecelerateInterpolator());
        }

        public void run() {
            if (mAnimator != this) return;

            if (mRestartAnimation) {
                mScroller.startScroll(0, (int) mStartCenterX, 0, (int) mEndCenterX, mAnimDuration);

                mRestartAnimation = false;
            }

            boolean scrollRemains = mScroller.computeScrollOffset();

            mAnimStartXPos = mAnimEndXPos;
            mAnimEndXPos = mScroller.getCurrY();

            if (scrollRemains) {
                invalidate();
                post(this);
            } else {
                stop();
                enableAnimationToCurrentState(false);
            }

        }

        public void start() {
            mRestartAnimation = true;
            postDelayed(this, mAnimStartDelay);
        }

        public void stop() {
            removeCallbacks(this);
            mAnimator = null;
        }

    }


    private float convertDpToPixel(float dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return dp * scale;
    }

    private float convertSpToPixel(float sp) {
        final float scale = getResources().getDisplayMetrics().scaledDensity;
        return sp * scale;
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        startAnimator();
    }


    @Override
    protected void onDetachedFromWindow() {
        stopAnimation();

        super.onDetachedFromWindow();
    }


    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        switch (visibility) {
            case View.VISIBLE:

                startAnimator();

                break;

            default:

                startAnimator();

                break;
        }
    }


    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(SUPER_STATE_KEY, super.onSaveInstanceState());

        bundle.putFloat(END_CENTER_X_KEY, this.mEndCenterX);

        bundle.putFloat(START_CENTER_X_KEY, this.mStartCenterX);

        bundle.putFloat(ANIM_START_X_POS_KEY, this.mAnimStartXPos);

        bundle.putFloat(ANIM_END_X_POS_KEY, this.mAnimEndXPos);

        bundle.putBoolean(IS_CURRENT_ANIM_STARTED_KEY, this.mIsCurrentAnimStarted);

        bundle.putBoolean(ANIMATE_TO_CURRENT_PROGRESS_STATE_KEY, this.mAnimateToCurrentProgressState);


        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;

            mEndCenterX = bundle.getFloat(END_CENTER_X_KEY);

            mStartCenterX = bundle.getFloat(START_CENTER_X_KEY);

            mAnimStartXPos = bundle.getFloat(ANIM_START_X_POS_KEY);

            mAnimEndXPos = bundle.getFloat(ANIM_END_X_POS_KEY);

            mIsCurrentAnimStarted = bundle.getBoolean(IS_CURRENT_ANIM_STARTED_KEY);

            mAnimateToCurrentProgressState = bundle.getBoolean(ANIMATE_TO_CURRENT_PROGRESS_STATE_KEY);

            state = bundle.getParcelable(SUPER_STATE_KEY);
        }
        super.onRestoreInstanceState(state);
    }

    private class State{
        private int value;
        private boolean filled;

        public State(int value, boolean filled) {
            this.value = value;
            this.filled = filled;
        }
    }
}
