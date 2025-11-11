package com.cigatee.gallery;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;

public class TypeWriterView extends androidx.appcompat.widget.AppCompatTextView {

    private CharSequence mText;
    private int mIndex;
    private long mDelay = 100; // delay in ms
    private Handler handler = new Handler();

    public TypeWriterView(Context context) {
        super(context);
    }

    public TypeWriterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TypeWriterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private final Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            setText(mText.subSequence(0, mIndex++));
            if (mIndex <= mText.length()) {
                handler.postDelayed(characterAdder, mDelay);
            }
        }
    };

    public void animateText(CharSequence text) {
        mText = text;
        mIndex = 0;
        setText("");
        handler.removeCallbacks(characterAdder);
        handler.postDelayed(characterAdder, mDelay);
    }

    public void setCharacterDelay(long millis) {
        mDelay = millis;
    }
}
