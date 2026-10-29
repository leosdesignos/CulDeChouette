package io.github.leosdesignos.culdechouette;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** Input for the value of a die: a single digit from 1 to 6, jumping to the next die once entered. */
public class DiceEditText extends androidx.appcompat.widget.AppCompatEditText {

    /** Rejects anything but 1 to 6, whether typed on any keyboard or pasted. */
    private static final InputFilter DIE_FILTER = (source, start, end, dest, dstart, dend) -> {
        for (int i = start; i < end; i++) {
            char c = source.charAt(i);
            if (c < '1' || c > '6') {
                return "";
            }
        }
        return null;
    };

    @Nullable
    private EditText nextEditText;

    public DiceEditText(@NonNull Context context) {
        super(context);
        init();
    }

    public DiceEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DiceEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.setInputType(InputType.TYPE_CLASS_NUMBER);
        this.setGravity(Gravity.CENTER);
        this.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1), DIE_FILTER});
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_large));
        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1 && hasFocus()) {
                    if (nextEditText != null) {
                        nextEditText.requestFocus();
                    } else {
                        hideKeyboard();
                    }
                }
            }
        });
    }

    public void jumpTo(EditText nextEditText) {
        this.nextEditText = nextEditText;
    }

    /** The value entered, 0 if empty. */
    public int value() {
        Editable text = getText();
        return text == null || text.length() == 0 ? 0 : text.charAt(0) - '0';
    }

    public boolean isEmpty() {
        return value() == 0;
    }

    public void focus() {
        if (requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void unfocus() {
        clearFocus();
        hideKeyboard();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindowToken(), 0);
    }
}
