package fh_dortmund_hagmans.einkauf;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import kr.nectarine.android.fruitygcm.receiver.FruityRegistrationIdUpdateReceiver;

/** Receiver, der regId Updates vom Google Server empfängt
 * @author Hendrik Hagmans
 */
public class GcmRegistrationUpdateReceiver extends FruityRegistrationIdUpdateReceiver {

    @Override
    public void onRegistrationIdRenewed(String regId) {
        Log.d("tag", "GcmRegistrationUpdateReceiver > onRegistrationIdRenewed : " + regId);
    }

    @Override
    public void onRegistrationIdRenewFailed() {
        Log.d("tag", "GcmRegistrationUpdateReceiver > onRegistrationIdRenewFailed : failed");
    }
}