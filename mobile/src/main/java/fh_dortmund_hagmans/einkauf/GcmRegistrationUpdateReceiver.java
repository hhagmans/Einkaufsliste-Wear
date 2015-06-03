package fh_dortmund_hagmans.einkauf;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import kr.nectarine.android.fruitygcm.receiver.FruityRegistrationIdUpdateReceiver;

/**
 * Created by hendrikh on 01.06.15.
 */
public class GcmRegistrationUpdateReceiver extends FruityRegistrationIdUpdateReceiver {

    @Override
    public void onRegistrationIdRenewed(String regId) {
        Log.d("tag", "GcmRegistrationUpdateReceiver > onRegistrationIdRenewed : " + regId);

    }

    @Override
    public void onRegistrationIdRenewFailed() {
        Log.d("tag", "GcmRegistrationUpdateReceiver > onRegistrationIdRenewFailed : failed");
        //needs extra backoff like retry later
    }
}