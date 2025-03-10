package com.example.culdechouette;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DiceEditText extends androidx.appcompat.widget.AppCompatEditText {

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
        this.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
        this.setOnKeyListener(new DiceKeyListener(null));
    }

    public void jumpTo(EditText nextEditText) {
        this.setOnKeyListener(new DiceKeyListener(nextEditText));
    }

    public int value() {
        Editable text = getText();
        return text == null ? -1 : Integer.parseInt(text.toString());
    }

    public boolean isEmpty() {
        Editable text = getText();
        return text == null || text.toString().isEmpty();
    }

    public void focus() {
        requestFocus();
        showKeyboard(this);
    }

    private void showKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private class DiceKeyListener implements View.OnKeyListener {

        private final EditText nextEditText;
        private final Set<Integer> allowedKeys = new HashSet<>(Arrays.asList(
                KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_3,
                KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_5, KeyEvent.KEYCODE_6
        ));

        public DiceKeyListener(EditText nextEditText) {
            this.nextEditText = nextEditText;
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (!allowedKeys.contains(keyCode) && keyCode != KeyEvent.KEYCODE_DEL) {
                    return true;
                }
                if (keyCode != KeyEvent.KEYCODE_DEL) {
                    if (nextEditText != null) {
                        nextEditText.requestFocus();
                    } else {
                        hideKeyboard(v);
                    }
                }
            }
            return false;
        }
    }
}
