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

/**
 * Main Activity, die immer auf der Wearable angezeigt wird
 * @author Hendrik Hagmans
 */
public class MainActivity extends Activity implements WearableListView.ClickListener, GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener{

    // Initiales Dataset der Liste
    Article[] elements = { new Article("Lädt aktuelle Liste...", Category.FLEISCHFISCH, 0)};
    GoogleApiClient mApiClient;
    ShoppingListAdapter adapter;
    private static final String INIT_LIST = "/init_list";
    private static final String CHECK_ARTICLE = "/check_article";

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Listen Komponente holen
    WearableListView listView =
            (WearableListView) findViewById(R.id.wearable_list);

    // Adapter der Liste zuweisen
    adapter = new ShoppingListAdapter(this, elements);
    listView.setAdapter(adapter);

    // Click Listener der Liste zuweisen
    listView.setClickListener(this);


    mApiClient = new GoogleApiClient.Builder(this)
            .addApi(Wearable.API)
            .build();

    mApiClient.connect();
    Wearable.MessageApi.addListener(mApiClient, this);
    // Initiale Liste von Smartphone holen
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
            final Article[] elements = {new Article(getString(R.string.loading_articles), Category.FLEISCHFISCH, 0)};
            runOnUiThread(new Runnable() {
                @Override
                public void run() { // Status "Laden" in der Liste darstellen
                    WearableListView listView =
                            (WearableListView) findViewById(R.id.wearable_list);
                    adapter.setmDataset(elements);
                    adapter.notifyDataSetChanged();
                }
            });
            // Aktuelle Liste von Smartphone holen
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
            final Article[] elements = {new Article(getString(R.string.loading_articles), Category.FLEISCHFISCH, 0)};
            runOnUiThread(new Runnable() {
                @Override
                public void run() { // Status "Laden" in der Liste darstellen
                    WearableListView listView =
                            (WearableListView) findViewById(R.id.wearable_list);
                    adapter.setmDataset(elements);
                    adapter.notifyDataSetChanged();
                }
            });
        // Aktuelle Liste von Smartphone holen
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
            final Article[] elements = {new Article(getString(R.string.loading_articles), Category.FLEISCHFISCH, 0)};
            runOnUiThread(new Runnable() {
                @Override
                public void run() { // Status "Laden" in der Liste darstellen
                    WearableListView listView =
                            (WearableListView) findViewById(R.id.wearable_list);
                    adapter.setmDataset(elements);
                    adapter.notifyDataSetChanged();
                }
            });
        // Aktuelle Liste von Smartphone holen
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sendMessage(INIT_LIST, "");
                }
            }).start();

    }

    /**
     *   Click Listener der Listview
     */
@Override
public void onClick(WearableListView.ViewHolder v) {
    final Integer tag = (Integer) v.itemView.getTag();
    final Integer position = (Integer) v.getPosition();
    if (tag.equals(0)) { // Kein Artikel, sondern Statuselement, daher Neuladen der Liste
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendMessage(INIT_LIST, "");
            }
        }).start();
    } else {
        // Artikel Id und checked Status auslesen
        Article[] dataset = adapter.getmDataset();
        final boolean checked = dataset[position].isChecked();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WearableListView listView =
                        (WearableListView) findViewById(R.id.wearable_list);
                Article[] dataset = adapter.getmDataset();
                // Checked Status des Artikels wechseln
                dataset[position].toggleArticle();
                // Neuen Checked Status in Liste anzeigen
                adapter.setmDataset(dataset);
                adapter.notifyDataSetChanged();
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() { // Checken des Artikels an Smartphone senden
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
        if( messageEvent.getPath().equalsIgnoreCase( INIT_LIST ) ) { // Aktuelle Liste empfangen
            byte[] payload = messageEvent.getData();
            String jsonString = new String(payload);
            Article[] articleArray = null;

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = null;
            try {
                articleArray = mapper.readValue(jsonString, Article[].class);
            } catch (IOException e) { // Wenn der Server keine (weil es keine gibt) oder keine valide Liste (warum auch immer) zurück gibt
                articleArray = new Article[1];
                articleArray[0] = new Article(getString(R.string.no_current_list), Category.FLEISCHFISCH, 0);
            }

            final Article[] viewArray = articleArray;
            runOnUiThread(new Runnable() {
                @Override
                public void run() { // Neue Artikelliste auf Wearable darstellen
                    WearableListView listView =
                            (WearableListView) findViewById(R.id.wearable_list);
                    adapter.setmDataset(viewArray);
                    adapter.notifyDataSetChanged();
                }
            });

            // Kurz vibrieren um den Nutzer auf die neue Liste aufmerksam zu machen
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(100);
        }
    }

    /**
     * Sendet eine Nachricht an die Smartphone App
     * @param message
     * @param payload
     */
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