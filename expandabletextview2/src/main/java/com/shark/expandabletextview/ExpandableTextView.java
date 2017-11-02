package com.shark.expandabletextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by panj on 2017/10/26.
 */

public class ExpandableTextView extends RelativeLayout implements View.OnClickListener {

    public static final int STATE_SHRINK = 0;
    public static final int STATE_EXPAND = 1;
    private int mCurrState = STATE_SHRINK;

    private static final int MAX_LINES_ON_SHRINK = 2;
    private static final boolean TOGGLE_ENABLE = true;
    private boolean mToggleEnable = TOGGLE_ENABLE;

    private String mOrigText;
    private int mTextColor;
    private String mExpandText;
    private String mShrinkText;
    private int mExpandTextColor;
    private int mMaxLinesOnShrink;
    private int mMarginLeft;

    private TextView mTextView;
    private TextView mExpandTextView;

    private Layout mLayout;
    private int mLayoutWidth = 0;
    private int mExpandWidth = 0;
    private TextPaint mTextPaint;
    private int mTextLineCount = -1;

    private OnExpandListener mOnExpandListener;

    public void setOnExpandListener(OnExpandListener onExpandListener) {
        mOnExpandListener = onExpandListener;
    }

    public interface OnExpandListener {
        void onExpand(View view);

        void onShrink(View view);
    }

    public ExpandableTextView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public ExpandableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public ExpandableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ExpandableTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ExpandableTextView, defStyleAttr, 0);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.ExpandableTextView_etv_Text) {
                mOrigText = a.getString(attr);
            } else if (attr == R.styleable.ExpandableTextView_etv_TextColor) {
                mTextColor = a.getColor(attr, Color.BLACK);
            } else if (attr == R.styleable.ExpandableTextView_etv_ExpandText) {
                mExpandText = a.getString(attr);
            } else if (attr == R.styleable.ExpandableTextView_etv_ExpandTextColor) {
                mExpandTextColor = a.getColor(attr, Color.BLACK);
            } else if (attr == R.styleable.ExpandableTextView_etv_ShrinkText) {
                mShrinkText = a.getString(attr);
            } else if (attr == R.styleable.ExpandableTextView_etv_MaxLinesOnShrink) {
                mMaxLinesOnShrink = a.getInteger(attr, MAX_LINES_ON_SHRINK);
            } else if (attr == R.styleable.ExpandableTextView_etv_State) {
                mCurrState = a.getInteger(attr, STATE_SHRINK);
            } else if (attr == R.styleable.ExpandableTextView_etv_ExpandTextMarginLeft) {
                mMarginLeft = a.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()));
            } else if (attr == R.styleable.ExpandableTextView_etv_EnableToggle) {
                mToggleEnable = a.getBoolean(attr, TOGGLE_ENABLE);
            }
        }
        a.recycle();
        if (mMaxLinesOnShrink <= 0) {
            mMaxLinesOnShrink = MAX_LINES_ON_SHRINK;
        }
        mTextView = new TextView(context);
        mTextView.setId(R.id.tv_original_text_view);
        mTextView.setTextColor(mTextColor);
        mTextPaint = mTextView.getPaint();

        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(mTextView, params);

        mExpandTextView = new TextView(context);
        mExpandTextView.setId(R.id.tv_expand_text_view);
        mExpandTextView.setTextColor(mExpandTextColor);
        mExpandTextView.setVisibility(GONE);

        LayoutParams params2 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params2.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.tv_original_text_view);
        params2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        addView(mExpandTextView, params2);
        showExpandText();
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver obs = getViewTreeObserver();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    obs.removeOnGlobalLayoutListener(this);
                } else {
                    obs.removeGlobalOnLayoutListener(this);
                }
                showText();
            }
        });
        if (mToggleEnable) {
            mExpandTextView.setOnClickListener(this);
        }
    }

    private String getNewTextByConfig() {
        if (TextUtils.isEmpty(mOrigText)) {
            return mOrigText;
        }
        mLayout = mTextView.getLayout();
        if (mLayout != null) {
            mLayoutWidth = mLayout.getWidth();
        }
        Layout layout = mExpandTextView.getLayout();
        if (layout != null) {
            mExpandWidth = layout.getWidth();
        }
        mTextLineCount = -1;
        mLayout = new DynamicLayout(mOrigText, mTextPaint, mLayoutWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        mTextLineCount = mLayout.getLineCount();
        if (mTextLineCount <= mMaxLinesOnShrink) {
            mExpandTextView.setVisibility(GONE);
            return mOrigText;
        } else {
            if (mExpandTextView != null) {
                if (mCurrState == STATE_SHRINK) {
                    mExpandTextView.setVisibility((TextUtils.isEmpty(mOrigText) || TextUtils.isEmpty(mExpandText)) ? GONE : VISIBLE);
                } else {
                    mExpandTextView.setVisibility((TextUtils.isEmpty(mOrigText) || TextUtils.isEmpty(mShrinkText)) ? GONE : VISIBLE);
                }
            }
        }

        switch (mCurrState) {
            case STATE_SHRINK: {
                mTextView.setLines(mTextLineCount);

                if (mTextLineCount <= mMaxLinesOnShrink) {
                    return mOrigText;
                }
                mTextView.setLines(mMaxLinesOnShrink);

                int indexEnd = mLayout.getLineEnd(mMaxLinesOnShrink - 1);
                int indexStart = mLayout.getLineStart(mMaxLinesOnShrink - 1);

                String newText = mOrigText.subSequence(indexStart, indexEnd).toString() + "...";
                int remainWidth = (int) (mTextPaint.measureText(newText) + 0.5);
                float maxWidthForLastLine = mLayoutWidth - mExpandWidth - mMarginLeft;

                while (remainWidth > maxWidthForLastLine) {
                    indexEnd--;
                    newText = mOrigText.subSequence(indexStart, indexEnd).toString() + "...";
                    remainWidth = (int) (mTextPaint.measureText(newText) + 0.5);
                }
                newText = mOrigText.subSequence(0, indexEnd).toString() + "...";
                return newText;
            }
            case STATE_EXPAND: {
                int indexEnd = mLayout.getLineEnd(mTextLineCount - 1);
                int indexStart = mLayout.getLineStart(mTextLineCount - 1);

                String newText = mOrigText.subSequence(indexStart, indexEnd).toString();
                int remainWidth = (int) (mTextPaint.measureText(newText) + 0.5);
                float maxWidthForLastLine = mLayoutWidth - mExpandWidth - mMarginLeft;

                if (remainWidth > maxWidthForLastLine) {
                    mTextView.setLines(mTextLineCount + 1);
                } else {
                    mTextView.setLines(mTextLineCount);
                }

                return mOrigText;
            }
        }
        return mOrigText;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_expand_text_view) {
            toggle();
        }
    }

    private void toggle() {
        switch (mCurrState) {
            case STATE_SHRINK:
                mCurrState = STATE_EXPAND;
                if (mOnExpandListener != null) {
                    mOnExpandListener.onExpand(this);
                }
                break;
            case STATE_EXPAND:
                mCurrState = STATE_SHRINK;
                if (mOnExpandListener != null) {
                    mOnExpandListener.onShrink(this);
                }
                break;
        }
        showExpandText();
        showText();
    }

    private void showText() {
        if (mTextView != null) {
            mTextView.setText(getNewTextByConfig());
        }
    }

    private void showExpandText() {
        if (mExpandTextView != null) {
            if (mCurrState == STATE_SHRINK) {
                mExpandTextView.setText(mExpandText);
            } else {
                mExpandTextView.setText(mShrinkText);
            }
            mExpandTextView.setVisibility(INVISIBLE);
        }
    }

    /**
     * Set content
     *
     * @param text
     */
    public void setText(String text) {
        mOrigText = text;
        showExpandText();
        mTextView.post(new Runnable() {
            @Override
            public void run() {
                showText();
            }
        });
    }

    public void setExpandText(String expandText) {
        mExpandText = expandText;
    }

    public void setShrinkText(String shrinkText) {
        mShrinkText = shrinkText;
    }

    public void setCurrState(int currState) {
        mCurrState = currState;
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
    }

    public void setExpandTextColor(int expandTextColor) {
        mExpandTextColor = expandTextColor;
    }

    public void setToggleEnable(boolean toggleEnable) {
        mToggleEnable = toggleEnable;
    }

    public void setMarginLeft(int marginLeft) {
        mMarginLeft = marginLeft;
    }

    public void setMaxLinesOnShrink(int maxLinesOnShrink) {
        mMaxLinesOnShrink = maxLinesOnShrink;
    }
}
