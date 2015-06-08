package fh_dortmund_hagmans.einkauf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
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

/** Main Activity, die immer auf dem Smartphone angezeigt wird
 * @author Hendrik Hagmans
 */
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

    /**
     * Überprüft asynchron, ob die Credentials gültig sind
     */
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

            if (currentUser != null) { // --> User angemeldet, Views unsichtbar machen
                usernameEdit.setVisibility(View.INVISIBLE);
                passwordEdit.setVisibility(View.INVISIBLE);
                loginButton.setVisibility(View.INVISIBLE);
                userNameText.setVisibility(View.INVISIBLE);
                passwordText.setVisibility(View.INVISIBLE);

                loginStatusText.setText(getString(R.string.logged_in_as) + " " + currentUser.getName());
                logoutButton.setVisibility(View.VISIBLE);
            } else { // --> User nicht angemeldet, loginStatus View anpassen
                loginStatusText.setText(getString(R.string.wrong_password));
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

        // User Credentials laden
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

            loginStatusText.setText(getString(R.string.logged_in_as) + " " + currentUser.getName());
            logoutButton.setVisibility(View.VISIBLE);
        }

        // GCM Client starten, registrieren und die regID setzen
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

    /**
     * Wird aufgerufen, wenn vom Server die Nachricht empfangen wurde, dass eine neue Liste vorhanden ist
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("MainActivity", "onNewIntent is called!");

        String message = intent.getStringExtra("message");

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String username = settings.getString("username", null);
        String password = settings.getString("password", null);

        if (username != null) {

            String answer = null;
            URL url = null;
            try {
                url = new URL(getString(R.string.server_url) + "login&name=" + username + "&password=" + password + "/check");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            BufferedReader in = null;
            try {
                in = new BufferedReader(
                        new InputStreamReader(url.openStream()));
                answer = in.readLine();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


            if (answer != null && answer.equals("true")) {
                sendMessageToWear(INIT_LIST, message);
            }
        }

        super.onNewIntent(intent);
    }

    /**
     * Aktualisiere View Variablen
     */
    public void setUpViews() {
        usernameEdit = (EditText) findViewById(R.id.usernameET);
        passwordEdit = (EditText) findViewById(R.id.passwordET);
        loginButton = (Button) findViewById(R.id.loginBtn);
        logoutButton = (Button) findViewById(R.id.logoutBtn);
        loginStatusText = (TextView) findViewById(R.id.loginStatus);
        userNameText = (TextView) findViewById(R.id.userNameText);
        passwordText = (TextView) findViewById(R.id.passwordText);
    }


    @Override
    protected void onResume() {
        super.onResume();

        setUpViews();

        // User Credentials laden
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

            loginStatusText.setText(getString(R.string.logged_in_as) + " " + currentUser.getName());
            logoutButton.setVisibility(View.VISIBLE);
        }

        // GCM Client starten, registrieren und die regID setzen
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
    }

    /**
     * Holt sich die eingegebenen Credentials und start den Login AsyncTask.
     * @param view
     */
    public void authenticateLogin(View view) {

        setUpViews();

        final String userName = usernameEdit.getText().toString();
        final String password = passwordEdit.getText().toString();

        currentUserName = userName;
        currentPassword = password;

        CheckLoginAsync task = new CheckLoginAsync();
        task.execute(new String[]{null});



    }

    /**
     * Loggt den Nutzer aus und setzt dementsprechend die Views wieder auf sichtbar/unsichtbar
     * @param view
     */
    public void logOut(View view) {
        setUpViews();

        currentUser = null;

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove("username");
        editor.remove("password");

        editor.commit();

        // Auf Wearable die aktuelle Liste löschen und Bitte aus Smartphone anmelden anzeigen
        new Thread(new Runnable() {
            @Override
            public void run() {
                Article[] elements = {new Article(getString(R.string.please_login), Category.FLEISCHFISCH, 0)};
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

        loginStatusText.setText(getString(R.string.not_logged_in));
        logoutButton.setVisibility(View.INVISIBLE);
    }

    /**
     * Öffnet die Register Webseite in externem Browser
     * @param view
     */
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

    /**
     * Sendet eine Nachricht an die Wearable
     * @param path
     * @param text
     */
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
