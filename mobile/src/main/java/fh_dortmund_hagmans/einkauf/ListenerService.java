package fh_dortmund_hagmans.einkauf;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

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

/**
 * Created by hendrikh on 24.04.15.
 */
public class ListenerService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks{

    private static final String INIT_LIST = "/init_list";
    private static final String CHECK_ARTICLE = "/check_article";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.v("TESTMOBILE", "Message Received");
        if( messageEvent.getPath().equalsIgnoreCase( INIT_LIST ) ) {
            String nodeId = messageEvent.getSourceNodeId();

            BufferedReader in = null;
            String inputLine = "";
            String json = "";
            try {
            URL url = new URL("https://einkauf.herokuapp.com/shoppingList/current/json");

            in = new BufferedReader(
                        new InputStreamReader(url.openStream()));

            while ((inputLine = in.readLine()) != null)
                json += inputLine;
            in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            sendMessage(INIT_LIST, json);
        } else if (messageEvent.getPath().equalsIgnoreCase( CHECK_ARTICLE )) {
            String payload = new String(messageEvent.getData());
            String[] splitPayload = payload.split(",");
            String id = splitPayload[0];
            boolean checked = Boolean.valueOf(splitPayload[1]);
            try {
                URL url = null;
                if (checked) {
                    url = new URL("https://einkauf.herokuapp.com/article&id=" + id + "/uncheck");
                }
                else {
                    url = new URL("https://einkauf.herokuapp.com/article&id=" + id + "/check");
                }
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(url.openStream()));
                in.readLine();
                in.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
        {
            super.onMessageReceived(messageEvent);
        }
    }

    private void sendMessage(String path, String message) {
        Log.v("TESTMOBILE", "Try sending message");
        Log.v("TESTMOBILE", message);
        GoogleApiClient client = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        client.blockingConnect(100, TimeUnit.MILLISECONDS);
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( client ).await();
        for(Node node : nodes.getNodes()) {
            Log.v("TESTMOBILE", node.getDisplayName());
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
