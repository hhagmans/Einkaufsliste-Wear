package fh_dortmund_hagmans.einkauf;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/** Receiver, der GCM Nachrichten vom Server empfängt
 * @author Hendrik Hagmans
 */
public class GcmMessageReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        String message = (String) extras.get("message");

        Log.d("tag", "GcmMessageReceiver > onReceive: " + message);

        // Nachricht an MainActivity zum Verschicken an Wearable weiterleiten
        Intent intent2open = new Intent(context, MainActivity.class);
        intent2open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent2open.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        String name = "message";
        String value = message;
        intent2open.putExtra(name, value);
        context.startActivity(intent2open);
    }

}