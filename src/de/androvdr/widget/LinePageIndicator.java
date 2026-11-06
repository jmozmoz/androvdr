package de.androvdr.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * A small ViewPager page indicator implemented in the app itself.
 * This replaces the unmaintained binary ViewPagerIndicator library, whose
 * compiled styleable indices are incompatible with current Android Gradle
 * resource processing.
 */
public class LinePageIndicator extends View implements ViewPager.OnPageChangeListener {
    private static final int SELECTED_COLOR = 0xFF33B5E5;
    private static final int UNSELECTED_COLOR = 0xFFBDBDBD;

    private final Paint selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint unselectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float lineWidth;
    private final float gapWidth;
    private final float strokeWidth;
    private ViewPager viewPager;
    private int currentPage;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Gewünschte Höhe der Linien: 10dp
        float density = getResources().getDisplayMetrics().density;
        int desiredHeight = (int) (10 * density + 0.5f)
                + getPaddingTop()
                + getPaddingBottom();

        int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = resolveSize(desiredHeight, heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    public LinePageIndicator(Context context) {
        this(context, null);
    }

    public LinePageIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LinePageIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        float density = getResources().getDisplayMetrics().density;
        lineWidth = 30f * density;
        gapWidth = 8f * density;
        strokeWidth = 4f * density;
        selectedPaint.setColor(SELECTED_COLOR);
        selectedPaint.setStrokeWidth(strokeWidth);
        selectedPaint.setStrokeCap(Paint.Cap.BUTT);
        unselectedPaint.setColor(UNSELECTED_COLOR);
        unselectedPaint.setStrokeWidth(strokeWidth);
        unselectedPaint.setStrokeCap(Paint.Cap.BUTT);
        setClickable(true);
        setMinimumHeight((int) (24f * density));
    }

    public void setViewPager(ViewPager pager) {
        setViewPager(pager, 0);
    }

    public void setViewPager(ViewPager pager, int initialPosition) {
        if (pager == null) {
            throw new IllegalArgumentException("ViewPager must not be null");
        }
        if (viewPager != null) {
            viewPager.removeOnPageChangeListener(this);
        }
        viewPager = pager;
        viewPager.addOnPageChangeListener(this);
        int count = viewPager.getAdapter() == null ? 0 : viewPager.getAdapter().getCount();
        if (count > 0) {
            currentPage = Math.max(0, Math.min(initialPosition, count - 1));
            if (viewPager.getCurrentItem() != currentPage) {
                viewPager.setCurrentItem(currentPage, false);
            }
        } else {
            currentPage = 0;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (viewPager == null || viewPager.getAdapter() == null) return;
        int count = viewPager.getAdapter().getCount();
        if (count <= 1) return;

        float totalWidth = count * lineWidth + (count - 1) * gapWidth;
        float x = (getWidth() - totalWidth) / 2f;
        float y = getPaddingTop() + (getHeight() - getPaddingTop() - getPaddingBottom()) / 2f;
        for (int i = 0; i < count; i++) {
            float start = x + i * (lineWidth + gapWidth);
            canvas.drawLine(start, y, start + lineWidth, y,
                    i == currentPage ? selectedPaint : unselectedPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (viewPager == null || viewPager.getAdapter() == null) return false;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int count = viewPager.getAdapter().getCount();
            if (count > 1) {
                float totalWidth = count * lineWidth + (count - 1) * gapWidth;
                float left = (getWidth() - totalWidth) / 2f;
                float relativeX = event.getX() - left;
                int page = Math.round(relativeX / (lineWidth + gapWidth));
                page = Math.max(0, Math.min(page, count - 1));
                viewPager.setCurrentItem(page, true);
                performClick();
            }
            return true;
        }
        return event.getAction() == MotionEvent.ACTION_DOWN
                || event.getAction() == MotionEvent.ACTION_MOVE;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

    @Override
    public void onPageSelected(int position) {
        currentPage = position;
        invalidate();
    }

    @Override
    public void onPageScrollStateChanged(int state) { }
}
