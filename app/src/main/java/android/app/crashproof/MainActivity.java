package android.app.crashproof;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

public final class MainActivity extends Activity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            makeAppCrash();
        }

        return super.onKeyDown(keyCode, event);
    }

    private void makeAppCrash() {
        throw new NullPointerException("This is an artificial exception");
    }
}
