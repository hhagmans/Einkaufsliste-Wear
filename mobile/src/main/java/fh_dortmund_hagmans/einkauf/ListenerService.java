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
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 * Created by hendrikh on 24.04.15.
 */
public class ListenerService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks{

    private static final String INIT_LIST = "/init_list";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.v("TESTMOBILE", "Message Received");
        if( messageEvent.getPath().equalsIgnoreCase( INIT_LIST ) ) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setContentTitle("My notification")
                            .setContentText("Hello World!");
// Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(this, MainActivity.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(MainActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
            int mId = 0;
            mNotificationManager.notify(mId, mBuilder.build());
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

            sendMessage(INIT_LIST, json, nodeId);
        } else {
            super.onMessageReceived(messageEvent);
        }
    }

    private void sendMessage(String path, String message, String nodeId) {
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
