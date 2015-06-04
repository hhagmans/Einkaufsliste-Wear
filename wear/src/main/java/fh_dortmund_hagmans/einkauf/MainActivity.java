package fh_dortmund_hagmans.einkauf;

import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Vibrator;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import fh_dortmund_hagmans.einkauf.models.Article;
import fh_dortmund_hagmans.einkauf.models.Category;
import fh_dortmund_hagmans.einkauf.models.ShoppingList;

public class MainActivity extends Activity implements WearableListView.ClickListener, GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener{

    // Sample dataset for the list
    Article[] elements = { new Article("Lädt aktuelle Liste...", Category.FLEISCHFISCH, 0)};
    GoogleApiClient mApiClient;
    ShoppingListAdapter adapter;
    private static final String INIT_LIST = "/init_list";
    private static final String CHECK_ARTICLE = "/check_article";
    private static final String SEND_USER = "/send_user";
    private static final String LOGOUT_USER = "/logout_user";

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
    new Thread(new Runnable() {
        @Override
        public void run() {
            sendMessage(INIT_LIST, "");
        }
    }).start();

}

@Override
public void onResume() {
        super.onResume();
            final Article[] elements = {new Article("Lädt aktuelle Liste...", Category.FLEISCHFISCH, 0)};
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    WearableListView listView =
                            (WearableListView) findViewById(R.id.wearable_list);
                    // Assign an adapter to the list
                    adapter.setmDataset(elements);
                    adapter.notifyDataSetChanged();
                }
            });
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sendMessage(INIT_LIST, "");
                }
            }).start();

    }

    @Override
    protected void onStart() {
        super.onStart();
            final Article[] elements = {new Article("Lädt aktuelle Liste...", Category.FLEISCHFISCH, 0)};
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    WearableListView listView =
                            (WearableListView) findViewById(R.id.wearable_list);
                    // Assign an adapter to the list
                    adapter.setmDataset(elements);
                    adapter.notifyDataSetChanged();
                }
            });
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sendMessage(INIT_LIST, "");
                }
            }).start();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
            final Article[] elements = {new Article("Lädt aktuelle Liste...", Category.FLEISCHFISCH, 0)};
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    WearableListView listView =
                            (WearableListView) findViewById(R.id.wearable_list);
                    // Assign an adapter to the list
                    adapter.setmDataset(elements);
                    adapter.notifyDataSetChanged();
                }
            });
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sendMessage(INIT_LIST, "");
                }
            }).start();

    }

// WearableListView click listener
@Override
public void onClick(WearableListView.ViewHolder v) {
    final Integer tag = (Integer) v.itemView.getTag();
    final Integer position = (Integer) v.getPosition();
    if (tag.equals(0)) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendMessage(INIT_LIST, "");
            }
        }).start();
    } else {
        Article[] dataset = adapter.getmDataset();
        final boolean checked = dataset[position].isChecked();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WearableListView listView =
                        (WearableListView) findViewById(R.id.wearable_list);
                // Assign an adapter to the list
                Article[] dataset = adapter.getmDataset();
                dataset[position].toggleArticle();
                adapter.setmDataset(dataset);
                adapter.notifyDataSetChanged();
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendMessage(CHECK_ARTICLE, tag.toString() + "," + checked);
            }
        }).start();
    }
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
            Article[] articleArray = null;

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = null;
            try {
                articleArray = mapper.readValue(jsonString, Article[].class);
            } catch (IOException e) {
                articleArray = new Article[1];
                articleArray[0] = new Article("Keine aktuelle Liste vorhanden. Klicken um nochmal zu laden.", Category.FLEISCHFISCH, 0);
            }

            final Article[] viewArray = articleArray;
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

            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(100);
        }
    }

    private void sendMessage(String message, String payload) {
        mApiClient.blockingConnect(100, TimeUnit.MILLISECONDS);
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mApiClient ).await();
        for(Node node : nodes.getNodes()) {
            if (payload == null) {
                Wearable.MessageApi.sendMessage(mApiClient, node.getId(), message, null);
            } else {
                Wearable.MessageApi.sendMessage(mApiClient, node.getId(), message, payload.getBytes());
            }
        }
    }
}