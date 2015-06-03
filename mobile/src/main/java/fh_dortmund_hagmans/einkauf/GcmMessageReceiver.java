package fh_dortmund_hagmans.einkauf;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by hendrikh on 01.06.15.
 */
public class GcmMessageReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        for (String s : extras.keySet()) {
            Log.d("tag", "GcmMessageReceiver > onReceive : " + s + " = " + extras.get(s));
        }
    }
}