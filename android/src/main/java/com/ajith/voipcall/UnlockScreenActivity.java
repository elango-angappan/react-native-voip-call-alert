package com.ajith.voipcall;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.squareup.picasso.Picasso;

import java.util.Timer;
import java.util.TimerTask;

public class UnlockScreenActivity extends AppCompatActivity {

    private static final String TAG = "MessagingService";

    private Context context;

    private TextView tvName;
    private TextView tvInfo;
    private ImageView ivAvatar;
    private Integer timeout = 0;
    private String uuid = "";
    static boolean active = false;
    //private static Vibrator v = (Vibrator) IncomingCallModule.reactContext.getSystemService(Context.VIBRATOR_SERVICE);
    private long[] pattern = {0, 1000, 800};
    //private static MediaPlayer player = MediaPlayer.create(IncomingCallModule.reactContext, Settings.System.DEFAULT_RINGTONE_URI);
    private static Activity fa;
    private Timer timer;

    int notificationId;
    String callerId, action, devId, notificationBody;
    RNVoipNotificationHelper rnVoipNotificationHelper;

    @Override
    public void onStart() {
        super.onStart();
        if (this.timeout > 0) {
              this.timer = new Timer();
              this.timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // this code will be executed after timeout seconds
                    dismissIncoming();
                }
            }, timeout);
        }
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fa = this;
        context = this;

        setContentView(R.layout.activity_call_incoming);

        rnVoipNotificationHelper = new RNVoipNotificationHelper((Application) getApplicationContext());
        notificationId = getIntent().getIntExtra("notificationId",0);
        callerId = getIntent().getStringExtra("callerId");
        devId = getIntent().getStringExtra("devId");
        notificationBody = getIntent().getStringExtra("notificationBody");
        action = getIntent().getStringExtra("action");

        tvName = findViewById(R.id.tvName);
        tvInfo = findViewById(R.id.tvInfo);
        ivAvatar = findViewById(R.id.ivAvatar);

        tvName.setText(notificationBody);
        /*Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey("notificationId")) {
                notificationId = bundle.getInt("notificationId");
            }
            if (bundle.containsKey("uuid")) {
                uuid = bundle.getString("uuid");
            }
            if (bundle.containsKey("name")) {
                String name = bundle.getString("name");
                tvName.setText(name);
            }
            if (bundle.containsKey("info")) {
                String info = bundle.getString("info");
                tvInfo.setText(info);
            }
            if (bundle.containsKey("avatar")) {
                String avatar = bundle.getString("avatar");
                if (avatar != null) {
                    Picasso.get().load(avatar).transform(new CircleTransform()).into(ivAvatar);
                }
            }
            if (bundle.containsKey("timeout")) {
                this.timeout = bundle.getInt("timeout");
            }
            else this.timeout = 0;
        }*/

        /*getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);*/

        //Picasso.get().load(R.drawable.ic_avatar_default).transform(new CircleTransform()).into(ivAvatar);

        turnScreenOnAndKeyguardOff();

        /*v.vibrate(pattern, 0);
        player.start();*/

        AnimateImage acceptCallBtn = findViewById(R.id.ivAcceptCall);
        acceptCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            try {
                /*v.cancel();
                player.stop();
                player.prepareAsync();*/
//                  acceptDialing();

//                  RNVoipRingtunePlayer.getInstance(UnlockScreenActivity.this).stopMusic();
//                  rnVoipNotificationHelper.clearNotification(notificationId);

                /*Intent intent = new Intent();
                getPendingIntent(notificationId, callerId, action).send(context.getApplicationContext(), 0, intent);*/

                WritableMap params = Arguments.createMap();
                params.putBoolean("accept", true);
                params.putString("uuid", uuid);
                params.putInt("notificationId", notificationId);
                params.putString("callerId", callerId);
                params.putString("devId", devId);
                sendEvent("answerCall", params);
                finish();
            } catch (Exception e) {
                WritableMap params = Arguments.createMap();
                params.putString("message", e.getMessage());
                //sendEvent("error", params);
                dismissDialing();
            }
            }
        });

        AnimateImage rejectCallBtn = findViewById(R.id.ivDeclineCall);
        rejectCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*v.cancel();
                player.stop();
                player.prepareAsync();*/
//                dismissDialing();
                /*RNVoipRingtunePlayer.getInstance(UnlockScreenActivity.this).stopMusic();
                rnVoipNotificationHelper.clearNotification(notificationId);*/

                WritableMap params = Arguments.createMap();
                params.putBoolean("accept", false);
                params.putString("uuid", uuid);
                params.putInt("notificationId", notificationId);
                params.putString("callerId", callerId);
                params.putString("devId", devId);
                sendEvent("endCall", params);
                finish();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        turnScreenOffAndKeyguardOn();
    }

    public PendingIntent getPendingIntent(int notificationID, String callerId, String type){
        Class intentClass = getMainActivityClass();
        Intent intent = new Intent(context.getApplicationContext(), intentClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("notificationId",notificationID);
        intent.putExtra("callerId", callerId);
        intent.putExtra("action", type);
        intent.setAction(type);
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), notificationID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    public Class getMainActivityClass() {
        String packageName = context.getPackageName();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        String className = launchIntent.getComponent().getClassName();
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        // Dont back
    }

    public void dismissIncoming() {
        /*v.cancel();
        player.stop();
        player.prepareAsync();*/
        dismissDialing();
    }

    private void acceptDialing() {
        WritableMap params = Arguments.createMap();
        params.putBoolean("accept", true);
        params.putString("uuid", uuid);
        if (timer != null){
          timer.cancel();
        }
        /*if (!IncomingCallModule.reactContext.hasCurrentActivity()) {
            params.putBoolean("isHeadless", true);
        }
        KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

        if (mKeyguardManager.isDeviceLocked()) {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mKeyguardManager.requestDismissKeyguard(this, new KeyguardManager.KeyguardDismissCallback() {
              @Override
              public void onDismissSucceeded() {
                super.onDismissSucceeded();
              }
            });
          }
        }

        sendEvent("answerCall", params);
        finish();*/
    }

    private void dismissDialing() {
        WritableMap params = Arguments.createMap();
        params.putBoolean("accept", false);
        params.putString("uuid", uuid);
        if (timer != null) {
          timer.cancel();
        }
        /*if (!IncomingCallModule.reactContext.hasCurrentActivity()) {
            params.putBoolean("isHeadless", true);
        }

        sendEvent("endCall", params);

        finish();*/
    }

    /*@Override
    public void onConnected() {
        Log.d(TAG, "onConnected: ");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "onDisconnected: ");

    }

    @Override
    public void onConnectFailure() {
        Log.d(TAG, "onConnectFailure: ");

    }

    @Override
    public void onIncoming(ReadableMap params) {
        Log.d(TAG, "onIncoming: ");
    }*/

    private void sendEvent(String eventName, WritableMap params) {
        RNVoipCallModule.reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    private void turnScreenOnAndKeyguardOff() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        }

//    with(getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            requestDismissKeyguard(this@turnScreenOnAndKeyguardOff, null)
//        }
//    }
    }

    private void turnScreenOffAndKeyguardOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(false);
            setTurnScreenOn(false);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        }
    }
}
