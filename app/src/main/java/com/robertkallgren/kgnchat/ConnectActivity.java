package com.robertkallgren.kgnchat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;


public class ConnectActivity extends Activity {

    // UI References.
    private EditText nickEditText;
    private EditText addressEditText;
    private EditText portEditText;
    private View loginFormView;
    private View progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        nickEditText = (EditText) findViewById(R.id.nick);
        addressEditText = (EditText) findViewById(R.id.address);
        portEditText = (EditText) findViewById(R.id.port);
        loginFormView = findViewById(R.id.login_form);
        progressView = findViewById(R.id.login_progress);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.connect, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Attempts to connect to the given address with the provided information.
     */
    public void connect(View view) {
        // Reset errors.
        nickEditText.setError(null);
        addressEditText.setError(null);
        portEditText.setError(null);

        // Store values at the time of the connection attempt.
        String nick = nickEditText.getText().toString();
        String address = addressEditText.getText().toString();
        int port;

        try {
            port = Integer.parseInt(portEditText.getText().toString());
        } catch (NumberFormatException e) {
            port = 0;
        }

        boolean cancel = false;
        View focusView = null;

        if (port == 0) {
            portEditText.setError(getString(R.string.error_invalid_port));
            focusView = portEditText;
            cancel = true;
        }

        if (TextUtils.isEmpty(address)) {
            addressEditText.setError(getString(R.string.error_field_required));
            focusView = addressEditText;
            cancel = true;
        }

        if (TextUtils.isEmpty(nick)) {
            nickEditText.setError(getString(R.string.error_field_required));
            focusView = nickEditText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            // TODO: Connect to server.
        }
    }

    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        loginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        progressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

}
