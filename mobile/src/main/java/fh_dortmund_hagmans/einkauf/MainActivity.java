package fh_dortmund_hagmans.einkauf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import fh_dortmund_hagmans.einkauf.models.Article;
import fh_dortmund_hagmans.einkauf.models.Category;
import fh_dortmund_hagmans.einkauf.models.User;
import kr.nectarine.android.fruitygcm.FruityGcmClient;
import kr.nectarine.android.fruitygcm.interfaces.FruityGcmListener;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks{

    GoogleApiClient mApiClient;

    private static final String INIT_LIST = "/init_list";
    private static final String PREFS_NAME = "prefs";

    private EditText usernameEdit;
    private EditText passwordEdit;
    private Button loginButton;
    private Button logoutButton;
    private TextView loginStatusText;
    private TextView userNameText;
    private TextView passwordText;

    private class CheckLoginAsync extends AsyncTask<String, String, String> {
        private EditText usernameEdit;
        private EditText passwordEdit;
        private Button loginButton;
        private Button logoutButton;
        private TextView loginStatusText;
        private TextView userNameText;
        private TextView passwordText;

        @Override
        protected String doInBackground(String... urls) {
            try {
            URL url = new URL(getString(R.string.server_url) + "login&name=" + currentUserName + "&password=" + User.encryptPassword(currentPassword) + "/check");
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(url.openStream()));

                String answer = in.readLine();
                in.close();
                if (answer != null && answer.equals("true")) {
                    currentUser = new User(currentUserName, currentPassword);

                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("username", currentUserName);
                    editor.putString("password", User.encryptPassword(currentPassword));
                    if (regKey != null) {
                        editor.putString("regKey", regKey);
                    }
                    editor.commit();



                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                URL url = new URL(getString(R.string.server_url) + "user&name=" + currentUserName + "&password=" + User.encryptPassword(currentPassword) + "/regid=" + regKey);
                                BufferedReader in = new BufferedReader(
                                        new InputStreamReader(url.openStream()));
                                in.readLine();
                                in.close();
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }).start();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            BufferedReader in = null;
                            String inputLine = "";
                            String json = "";
                            try {
                                URL url = new URL(getString(R.string.server_url) + "shoppingList/current/json&username=" + currentUserName + "&password=" + User.encryptPassword(currentPassword));

                                in = new BufferedReader(
                                        new InputStreamReader(url.openStream()));

                                while ((inputLine = in.readLine()) != null)
                                    json += inputLine;
                                in.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            sendMessageToWear(INIT_LIST, json);
                        }

                        }).start();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            setUpViews();

        }

        @Override
        protected void onPostExecute(String result) {
            setUpViews();

            if (currentUser != null) {
                usernameEdit.setVisibility(View.INVISIBLE);
                passwordEdit.setVisibility(View.INVISIBLE);
                loginButton.setVisibility(View.INVISIBLE);
                userNameText.setVisibility(View.INVISIBLE);
                passwordText.setVisibility(View.INVISIBLE);

                loginStatusText.setText("Eingeloggt als " + currentUser.getName());
                logoutButton.setVisibility(View.VISIBLE);
            } else {
                loginStatusText.setText("Falsches Passwort/Username");
            }
        }

        public void setUpViews() {
            usernameEdit = (EditText) findViewById(R.id.usernameET);
            passwordEdit = (EditText) findViewById(R.id.passwordET);
            loginButton = (Button) findViewById(R.id.loginBtn);
            logoutButton = (Button) findViewById(R.id.logoutBtn);
            loginStatusText = (TextView) findViewById(R.id.loginStatus);
            userNameText = (TextView) findViewById(R.id.userNameText);
            passwordText = (TextView) findViewById(R.id.passwordText);
        }
    }

    private User currentUser = null;
    private String currentUserName;
    private String currentPassword;
    private String regKey = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .build();

        mApiClient.connect();

        setContentView(R.layout.activity_main);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        currentUserName = settings.getString("username", null);
        currentPassword = settings.getString("password", null);
        regKey = settings.getString("regKey", null);
        if (currentUserName != null) {
            setUpViews();
            currentUser = new User(currentUserName, currentPassword);
            usernameEdit.setVisibility(View.INVISIBLE);
            passwordEdit.setVisibility(View.INVISIBLE);
            loginButton.setVisibility(View.INVISIBLE);
            userNameText.setVisibility(View.INVISIBLE);
            passwordText.setVisibility(View.INVISIBLE);

            loginStatusText.setText("Eingeloggt als " + currentUser.getName());
            logoutButton.setVisibility(View.VISIBLE);
        }

        FruityGcmClient.start(this, getString(R.string.sender_id), new FruityGcmListener() {
            @Override
            public void onPlayServiceNotAvailable(boolean didPlayHandleError) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("TEST", "play not available");
                    }
                });
            }

            @Override
            public void onDeliverRegistrationId(final String regId, boolean isNew) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Log.d("TEST", "MainActivity > run: " + regId);
                        regKey = regId;
                    }
                });
            }

            @Override
            public void onRegisterFailed() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("TEST", "reg failed");
                    }
                });
            }
        });
    }

    public void setUpViews() {
        usernameEdit = (EditText) findViewById(R.id.usernameET);
        passwordEdit = (EditText) findViewById(R.id.passwordET);
        loginButton = (Button) findViewById(R.id.loginBtn);
        logoutButton = (Button) findViewById(R.id.logoutBtn);
        loginStatusText = (TextView) findViewById(R.id.loginStatus);
        userNameText = (TextView) findViewById(R.id.userNameText);
        passwordText = (TextView) findViewById(R.id.passwordText);
    }


    // You need to do the Play Services APK check here too.
    @Override
    protected void onResume() {
        super.onResume();

        setUpViews();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        currentUserName = settings.getString("username", null);
        currentPassword = settings.getString("password", null);
        regKey = settings.getString("regKey", null);
        if (currentUserName != null) {
            currentUser = new User(currentUserName, currentPassword);
            usernameEdit.setVisibility(View.INVISIBLE);
            passwordEdit.setVisibility(View.INVISIBLE);
            loginButton.setVisibility(View.INVISIBLE);
            userNameText.setVisibility(View.INVISIBLE);
            passwordText.setVisibility(View.INVISIBLE);

            loginStatusText.setText("Eingeloggt als " + currentUser.getName());
            logoutButton.setVisibility(View.VISIBLE);
        }

        FruityGcmClient.start(this, "1047632849901", new FruityGcmListener() {
            @Override
            public void onPlayServiceNotAvailable(boolean didPlayHandleError) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("TEST", "play not available");
                    }
                });
            }

            @Override
            public void onDeliverRegistrationId(final String regId, boolean isNew) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Log.d("tag", "MainActivity > run: " + regId);
                        regKey = regId;
                    }
                }).start();
            }

            @Override
            public void onRegisterFailed() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("TEST", "reg failed");
                    }
                });
            }
        });
        Log.v("TEST", FruityGcmClient.REGISTRATION_ID);
    }

    public void authenticateLogin(View view) {

        setUpViews();

        final String userName = usernameEdit.getText().toString();
        final String password = passwordEdit.getText().toString();

        currentUserName = userName;
        currentPassword = password;

        CheckLoginAsync task = new CheckLoginAsync();
        task.execute(new String[]{null});



    }

    public void logOut(View view) {
        setUpViews();

        currentUser = null;

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove("username");
        editor.remove("password");

        editor.commit();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Article[] elements = {new Article("Bitte erst auf Smartphone anmelden", Category.FLEISCHFISCH, 0)};
                ObjectMapper mapper = new ObjectMapper();
                String json = "";
                try {
                    json = mapper.writeValueAsString(elements);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                sendMessageToWear(INIT_LIST, json);
            }



        }).start();

        usernameEdit.setVisibility(View.VISIBLE);
        passwordEdit.setVisibility(View.VISIBLE);
        userNameText.setVisibility(View.VISIBLE);
        passwordText.setVisibility(View.VISIBLE);
        loginButton.setVisibility(View.VISIBLE);

        loginStatusText.setText("Nicht eingeloggt");
        logoutButton.setVisibility(View.INVISIBLE);
    }

    public void openRegister(View view) {
        String url = null;
        url = getString(R.string.server_url) + "register";

        if (!url.startsWith("https://") && !url.startsWith("http://")){
            url = "http://" + url;
        }
        Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(openUrlIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mApiClient.disconnect();
    }

    private void sendMessageToWear(final String path, final String text) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mApiClient ).await();
                for(Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mApiClient, node.getId(), path, text.getBytes() ).await();
                }

            }
        }).start();
    }
}
