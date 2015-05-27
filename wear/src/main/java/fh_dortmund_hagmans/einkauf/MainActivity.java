package fh_dortmund_hagmans.einkauf;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import fh_dortmund_hagmans.einkauf.models.Article;
import fh_dortmund_hagmans.einkauf.models.ShoppingList;

public class MainActivity extends Activity implements WearableListView.ClickListener, GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener{

    // Sample dataset for the list
    String[] elements = { "Keine aktuelle Liste vorhanden"};
    GoogleApiClient mApiClient;
    ShoppingListAdapter adapter;
    private static final String INIT_LIST = "/init_list";

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Get the list component from the layout of the activity
    WearableListView listView =
            (WearableListView) findViewById(R.id.wearable_list);

    // Assign an adapter to the list
    adapter = new ShoppingListAdapter(this, elements);
    listView.setAdapter(adapter);

    // Set a click listener
    listView.setClickListener(this);


    mApiClient = new GoogleApiClient.Builder(this)
            .addApi(Wearable.API)
            .build();

    mApiClient.connect();
    Wearable.MessageApi.addListener(mApiClient, this);
}

// WearableListView click listener
@Override
public void onClick(WearableListView.ViewHolder v) {
        Integer tag = (Integer) v.itemView.getTag();
    new Thread(new Runnable() {
        @Override
        public void run() {
            sendMessage(INIT_LIST, "");
        }
    }).start();
        // use this data to complete some action ...
        }

@Override
public void onTopEmptyRegionClick() {
        }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.v("TEST", "Message Received");
        if( messageEvent.getPath().equalsIgnoreCase( INIT_LIST ) ) {
            byte[] payload = messageEvent.getData();
            String jsonString = new String(payload);
            Log.v("TEST", jsonString);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = null;
            ShoppingList list = null;
            try {
                list = mapper.readValue(jsonString, ShoppingList.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            final String[] articleArray = new String[list.getArticles().size()];
            int i = 0;
            for (Article article: list.getArticles()) {
                articleArray[i] = article.getName();
                i++;
            }
            final String[] viewArray = articleArray;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    WearableListView listView =
                            (WearableListView) findViewById(R.id.wearable_list);
                    // Assign an adapter to the list
                    adapter.setmDataset(viewArray);
                    adapter.notifyDataSetChanged();
                }
        });


        }
    }

    private void sendMessage(String message, String payload) {
        mApiClient.blockingConnect(100, TimeUnit.MILLISECONDS);
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mApiClient ).await();
        for(Node node : nodes.getNodes()) {
            Log.v("TEST", node.getDisplayName());
            Wearable.MessageApi.sendMessage(mApiClient, node.getId(), message, null);
        }
    }
}