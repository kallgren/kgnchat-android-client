package com.robertkallgren.kgnchat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class ConnectActivity extends Activity {

    // Used for logging.
    public static final String TAG = "CONNECTACTIVITY";

    // Keep track of the connect task to ensure we can cancel it if requested.
    private ConnectTask connectTask = null;

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
            connectTask = new ConnectTask(nick, address, port);
            connectTask.execute((Void) null);
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

    /**
     * Represents an asynchronous task used to connect to the server.
     */
    public class ConnectTask extends AsyncTask<Void, Void, Integer> {
        // Error codes.
        private static final int NETWORK_ERROR = 1;
        private static final int NICK_IN_USE = 2;

        private final String nick;
        private final String address;
        private final int port;

        ConnectTask(String nick, String address, int port) {
            this.nick = nick;
            this.address = address;
            this.port = port;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            Log.d(TAG, "Attempting to setup a connection to server");
            try {
                InetAddress inetAddress = InetAddress.getByName(address);

                Log.d(TAG, "Creating socket to " + inetAddress.getHostAddress() + ":" + port);
                Socket connection = new Socket(inetAddress, port);
                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));

                Log.d(TAG, "Sending connection request");
                out.writeBytes("CONNECT:" + nick + ":KGN Chat Android Client\n");
                String response = in.readLine();
                Log.d(TAG, "Received response: " + response);

                if (response.equals("ERROR:Nick in use")) {
                    connection.close();
                    return NICK_IN_USE;
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
                return NETWORK_ERROR;
            } catch (IOException e) {
                e.printStackTrace();
                return NETWORK_ERROR;
            }

            return 0;
        }

        @Override
        protected void onPostExecute(final Integer result) {
            connectTask = null;
            showProgress(false);

            switch (result) {
                case NETWORK_ERROR:
                    addressEditText.setError(getString(R.string.error_connection_failed));
                    addressEditText.requestFocus();
                    Log.d(TAG, "Connection failed, displaying 'network error' message");
                    break;
                case NICK_IN_USE:
                    nickEditText.setError(getString(R.string.error_nick_in_use));
                    nickEditText.requestFocus();
                    Log.d(TAG, "Connection failed, displaying 'nick in use' message");
                    break;
                default:
                    Log.d(TAG, "Connection succeeded, launching chat activity");
                    // TODO: Launch chat activity.
                    // finish();
            }
        }

        @Override
        protected void onCancelled() {
            connectTask = null;
            showProgress(false);
            Log.d(TAG, "Connection cancelled");
        }
    }

}
