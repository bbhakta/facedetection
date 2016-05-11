package facetracker.view;

import facetracker.R;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class AntiFatigueActivity extends ListActivity {

    private static final int CONTACTS_ACCESS = 1;
    private static final int PHONE_ACCESS = 2;

    private boolean hasContactPermission = false;
    private boolean canCall = false;
    private boolean isPhoneCalling = false;
    //private ListView lv;
    //private Cursor c;
    private SimpleCursorAdapter sadapt;

    MediaPlayer alertSound;
    MediaPlayer alertSound2;


    @Override
    public long getSelectedItemId() {
        return super.getSelectedItemId();

    }

    @Override
    public int getSelectedItemPosition() {
        return super.getSelectedItemPosition();
    }

    ListView lv;
    Cursor c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anti_fatigue);


        /*requests permissions*/
        //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, PHONE_ACCESS);
        //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, CONTACTS_ACCESS);

        alertSound = MediaPlayer.create(this, R.raw.alert);
        alertSound.setLooping(true);
        alertSound2 = MediaPlayer.create(this, R.raw.siren);
        alertSound2.setLooping(true);

        alertSound.start();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                alertSound.stop();
                alertSound2.start();
            }

        }, 9000);

        Button stopAlertButton = (Button) findViewById(R.id.stopAlert);
        stopAlertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (alertSound.isLooping())
                    alertSound.stop();
                if (alertSound2.isLooping())
                    alertSound2.stop();


                Intent in = new Intent(AntiFatigueActivity.this, FaceTrackerActivity.class);
                startActivity(in);
                finish();
            }
        });


        Button makeCallButton = (Button) findViewById(R.id.makeCall);

        makeCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (true) {
                    if (alertSound.isLooping())
                        alertSound.stop();
                    if (alertSound2.isLooping())
                        alertSound2.stop();
                    getContacts();
                } else {
                    Toast.makeText(getBaseContext(), "poop", Toast.LENGTH_SHORT).show();
                    if (alertSound.isLooping())
                        alertSound.stop();
                    if (alertSound2.isLooping())
                        alertSound2.stop();
                }
            }
        });

        /*Phone State checking*/
        PhoneCallListener phone = new PhoneCallListener();
        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        manager.listen(phone, PhoneStateListener.LISTEN_CALL_STATE);

    }

    /*
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CONTACTS_ACCESS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasContactPermission = true;
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    hasContactPermission = false;
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }

            case PHONE_ACCESS: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    canCall = true;
                }
                else {
                    canCall = false;
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }*/

    @SuppressLint("InlinedApi")
    public void getContacts() {
        try {
            /*gets contact info for all favorite contacts*/
            c = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, "starred=1", null, null);
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
            //tv.setText(e.toString());
        }

        final String[] from = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone._ID};
        int[] to = {android.R.id.text1, android.R.id.text2};

        sadapt = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, c, from, to, 0);
        setListAdapter(sadapt);

        lv = getListView();
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(Intent.ACTION_CALL);

                /* Gets number string from selected contact */
                sadapt = (SimpleCursorAdapter) parent.getAdapter();
                c = sadapt.getCursor();
                String number = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                /* Filters all non-numeric numbers from number */
                number = number.replaceAll("[^0-9.]", "");


                Toast.makeText(getBaseContext(), number, Toast.LENGTH_SHORT).show();
                intent.setData(Uri.parse("tel:" + number));

                if(true) {
                    startActivity(intent);
                }
                else {
                    Toast.makeText(getBaseContext(), "Needs Call permission", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //monitor phone call activities
    private class PhoneCallListener extends PhoneStateListener {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            if (TelephonyManager.CALL_STATE_RINGING == state) {

            }

            if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
                isPhoneCalling = true;
            }

            if (TelephonyManager.CALL_STATE_IDLE == state) {
                // run when class initial and phone call ended,
                // need detect flag from CALL_STATE_OFFHOOK
                if (isPhoneCalling) {
                    // restart app
                   /* Intent i = getBaseContext().getPackageManager()
                            .getLaunchIntentForPackage(
                                    getBaseContext().getPackageName());
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);*/
                    Toast.makeText(getBaseContext(), "Call Ended", Toast.LENGTH_SHORT).show();
                    isPhoneCalling = false;
                }

            }
        }
    }

}
