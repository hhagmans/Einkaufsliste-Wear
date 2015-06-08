package fh_dortmund_hagmans.einkauf;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import fh_dortmund_hagmans.einkauf.models.Article;
import fh_dortmund_hagmans.einkauf.models.Category;
import fh_dortmund_hagmans.einkauf.models.User;

/** Service, der auf Nachrichten von der Wearable hört und Antworten zurück schickt
 * @author Hendrik Hagmans
 */
public class ListenerService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks{

    private static final String INIT_LIST = "/init_list";
    private static final String CHECK_ARTICLE = "/check_article";
    private static final String PREFS_NAME = "prefs";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if( messageEvent.getPath().equalsIgnoreCase( INIT_LIST ) ) { // Artikelliste auf Wearable aktualisieren
            String nodeId = messageEvent.getSourceNodeId();

            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            String username = settings.getString("username", null);
            String password = settings.getString("password", null);

            if (username != null) { // Prüfen ob Nutzer auf Smartphone angemeldet ist

                String answer = null;
                URL url = null;
                // Prüfen ob Anmeldung auf Smartphone korrekt
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


                if (answer == null || answer.equals("true") == false) { // Anmeldung auf Smartphone falsch
                    SharedPreferences.Editor editor = settings.edit();
                    editor.remove("username");
                    editor.remove("password");

                    editor.commit();

                    Article[] elements = {new Article(getString(R.string.please_login), Category.FLEISCHFISCH, 0)};
                    ObjectMapper mapper = new ObjectMapper();
                    String json = "";
                    try {
                        json = mapper.writeValueAsString(elements);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    sendMessage(INIT_LIST, json);
                } else {

                    String inputLine = "";
                    String json = "";
                    try { // Aktuelle Liste des Users holen
                        url = new URL(getString(R.string.server_url) + "shoppingList/current/json&username=" + username + "&password=" + password);

                        in = new BufferedReader(
                                new InputStreamReader(url.openStream()));

                        while ((inputLine = in.readLine()) != null)
                            json += inputLine;
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    sendMessage(INIT_LIST, json);
                }
            } else { // Nutzer ist nicht auf Smartphone angemeldet
                Article[] elements = {new Article(getString(R.string.please_login), Category.FLEISCHFISCH, 0)};
                ObjectMapper mapper = new ObjectMapper();
                String json = "";
                try {
                    json = mapper.writeValueAsString(elements);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                sendMessage(INIT_LIST, json);
            }
        } else if (messageEvent.getPath().equalsIgnoreCase( CHECK_ARTICLE )) { // Artikel, der auf Wearbale geklickt wurde, aktualisieren
            String payload = new String(messageEvent.getData());
            Log.v("TEST", payload);
            String[] splitPayload = payload.split(",");
            String id = splitPayload[0];
            boolean checked = Boolean.valueOf(splitPayload[1]);

            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            String username = settings.getString("username", null);
            String password = settings.getString("password", null);

            if (username != null) { // Prüfen ob Nutzer auf Smartphone angemeldet ist
                String answer = null;
                URL url = null;
                try { // Prüfen ob Anmeldung auf Smartphone korrekt
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


                if (answer == null || answer.equals("true") == false) { // Anmeldung auf Smartphone falsch
                SharedPreferences.Editor editor = settings.edit();
                editor.remove("username");
                editor.remove("password");

                editor.commit();

                Article[] elements = {new Article(getString(R.string.please_login), Category.FLEISCHFISCH, 0)};
                ObjectMapper mapper = new ObjectMapper();
                String json = "";
                try {
                    json = mapper.writeValueAsString(elements);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                sendMessage(INIT_LIST, json);
            } else {
                try {
                    if (checked) { // Artikel bisher gecheckt, daher jetzt unchecken
                        url = new URL(getString(R.string.server_url) + "article&id=" + id + "/uncheck&username=" + username + "&password=" + password);
                    } else { // Artikel bisher ungecheckt, daher jetzt checken
                        url = new URL(getString(R.string.server_url) + "article&id=" + id + "/check&username=" + username + "&password=" + password);
                    }
                    in = new BufferedReader(
                            new InputStreamReader(url.openStream()));
                    in.readLine();
                    in.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            }
        } else
        {
            super.onMessageReceived(messageEvent);
        }
    }

    /**
     * Sendet eine Nachricht an die Wearable
     * @param path
     * @param message
     */
    private void sendMessage(String path, String message) {
        Log.v("TESTMOBILE", "Try sending message");
        GoogleApiClient client = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        client.blockingConnect(100, TimeUnit.MILLISECONDS);
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( client ).await();
        for(Node node : nodes.getNodes()) {
            Wearable.MessageApi.sendMessage(client, node.getId(), path, message.getBytes());
        }
        client.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
