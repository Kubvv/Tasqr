/* Multiple state checkbox (was tri state before) */
package com.example.tasqr.Styling;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.example.tasqr.R;

public class CheckBoxTriState extends androidx.appcompat.widget.AppCompatCheckBox {
    static private final int PENDING = 0;
    static private final int REVIEW = 1;
    static private final int DONE = 2;
    static private final int ABANDONED = 3;
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

            /* Checkbox status is changed from uncheck to checked */
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switch (state) {
                    default:
                    case PENDING:
                        state = REVIEW;
                        break;
                    case REVIEW:
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
        int btnDrawable = R.drawable.working;
        switch (state) {
            default:
            case PENDING:
                btnDrawable = R.drawable.working;
                break;
            case DONE:
                btnDrawable = R.drawable.ok;
                break;
            case ABANDONED:
                btnDrawable = R.drawable.notok;
                break;
            case REVIEW:
                btnDrawable = R.drawable.reviewing;
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