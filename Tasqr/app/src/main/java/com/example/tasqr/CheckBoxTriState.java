/*
*   CURRENTLY NOT USED
*
* */
package com.example.tasqr;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class CheckBoxTriState extends androidx.appcompat.widget.AppCompatCheckBox {
    static private final int PENDING = -1;
    static private final int DONE = 0;
    static private final int ABANDONED = 1;
    private int state;

    public CheckBoxTriState(Context context) {
        super(context);
        init();
    }

    public CheckBoxTriState(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CheckBoxTriState(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        state = PENDING;
        updateBtn();

        setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            // checkbox status is changed from uncheck to checked.
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switch (state) {
                    default:
                    case PENDING:
                        state = DONE;
                        break;
                    case DONE:
                        state = ABANDONED;
                        break;
                    case ABANDONED:
                        state = PENDING;
                        break;
                }
                updateBtn();
            }
        });
    }

    private void updateBtn() {
        int btnDrawable = R.drawable.ic_baseline_carpenter_24;
        switch (state) {
            default:
            case PENDING:
                btnDrawable = R.drawable.ic_baseline_carpenter_24;
                break;
            case DONE:
                btnDrawable = R.drawable.ic_baseline_check_circle_outline_24;
                break;
            case ABANDONED:
                btnDrawable = R.drawable.ic_baseline_clear_24;
                break;
        }

        setButtonDrawable(btnDrawable);
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
        updateBtn();
    }
}