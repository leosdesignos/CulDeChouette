package io.github.leosdesignos.culdechouette;

import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

/**
 * Puts every screen below an app bar and keeps it clear of the system bars and of the keyboard:
 * from Android 15, apps are drawn edge to edge, behind the status and navigation bars.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.activity_frame);
        ViewGroup content = findViewById(R.id.frameContent);
        getLayoutInflater().inflate(layoutResID, content, true);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        View appBar = findViewById(R.id.appBar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.frameRoot), (root, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            Insets keyboard = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            // The app bar extends behind the status bar
            appBar.setPadding(bars.left, bars.top, bars.right, 0);
            content.setPadding(bars.left, 0, bars.right, Math.max(bars.bottom, keyboard.bottom));
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
