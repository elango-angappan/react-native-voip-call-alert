package com.ajith.voipcall;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.bridge.CatalystInstance;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableNativeArray;

import java.text.ParseException;

public class RNOperationHelper {

    public enum Operation implements Parcelable {
        GET_MESSAGE_IMAGE;

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(ordinal());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Operation> CREATOR = new Creator<Operation>() {
            @Override
            public Operation createFromParcel(Parcel in) {
                return Operation.values()[in.readInt()];
            }

            @Override
            public Operation[] newArray(int size) {
                return new Operation[size];
            }
        };
    }

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 101;
    private static final int REQUEST_PERMISSION_SETTING = 102;

    private ReactApplication reactApp;
    private AppCompatActivity activity;
    private OperationCallback callback;

    public RNOperationHelper(ReactApplication reactApp, AppCompatActivity appCompatActivity, OperationCallback callback) {
        this.reactApp = reactApp;
        this.activity = appCompatActivity;
        this.callback = callback;
    }

    public interface OperationCallback {

        public void onSuccess(String message);

        public void onFailure(String message);

        public void foundLock(ReadableMap lock);
    }

    public void performOperation(Operation operation, String deviceId) {  // , Schedule deviceSchedule
        try {
            switch (operation) {
                case GET_MESSAGE_IMAGE:
                    doOperation(reactApp, operation, "getDeviceMessageImage", getParamsForWrite(Operation.GET_MESSAGE_IMAGE, deviceId));
                    break;
            }
        } catch (Exception exception) {
            callback.onFailure(exception.getMessage());
        }
    }


    void doOperation(ReactApplication app, final Operation operation, String methodName, WritableNativeArray tobeWritten) {
        //if (BluetoothUtility.isBluetoothEnabled() && isLocationPermissionGranted() && BluetoothUtility.isGpsEnabled(activity)) {
            try {
                //ReactNativeHost reactNativeHost = Controller.getInstance().getReactNativeHost();
                ReactNativeHost reactNativeHost = app.getReactNativeHost();
                ReactInstanceManager reactInstanceManager = reactNativeHost.getReactInstanceManager();
                ReactContext reactContext = reactInstanceManager.getCurrentReactContext();

                if (reactContext != null) {
                    Log.d("ReactContext : ", "reactContext.hasActiveCatalystInstance() : "+reactContext.hasActiveCatalystInstance() + "");

                    /*reactContext.setNativeModuleCallExceptionHandler(new NativeModuleCallExceptionHandler() {
                        @Override
                        public void handleException(Exception e) {
                            LogCat.e("ReactContext : ", "NativeModuleCallExceptionHandler : "+e.getMessage() + "");
                            e.printStackTrace();
                        }
                    });*/

                    if(!reactContext.hasActiveCatalystInstance()) {
                        reactInstanceManager.recreateReactContextInBackground();
                        Thread.sleep(2000);

                        reactNativeHost = app.getReactNativeHost();
                        reactInstanceManager = reactNativeHost.getReactInstanceManager();
                        reactContext = reactInstanceManager.getCurrentReactContext();
                    }

                    /*CatalystInstance catalystInstance = reactContext.getCatalystInstance();
                    WritableNativeArray params = new WritableNativeArray();
                    //params.pushString("Set Extra Message was called!");
                    //params.pushString(NetworkingUrls.BASEURL);
                    catalystInstance.callFunction("DahaoLockOperation", "setServerUrl", params);*/
                } else {
                    reactInstanceManager.createReactContextInBackground();
                    /*try {
                        reactInstanceManager.createReactContextInBackground();
                    } catch (Exception e) {
                        Utility.printStackTrace(e);
                        reactInstanceManager.recreateReactContextInBackground();
                    }*/
                }

                if (reactContext != null) {
                    Log.d("ReactContext : ", "reactContext.hasActiveCatalystInstance() : "+reactContext.hasActiveCatalystInstance() + "");

                    CatalystInstance catalystInstance = reactContext.getCatalystInstance();
                    catalystInstance.getNativeModule(VoipModule.class).setRnOperationCallback(new VoipModule.RNOperationCallback() {

                        @Override
                        public void onSuccess(String message) {
                            callback.onSuccess(message);
                        }

                        @Override
                        public void onFailure(String message) {
                            callback.onFailure(message);
                        }
                    });
                    catalystInstance.callFunction("RNOperation", methodName, tobeWritten);
                } else {
                    reactInstanceManager.createReactContextInBackground();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    private WritableNativeArray getParamsForWrite(Operation operation, String devId) throws ParseException {
        WritableNativeArray params = new WritableNativeArray();
        switch (operation) {
            case GET_MESSAGE_IMAGE:
                params.pushString(devId);
                break;
        }

        return params;
    }
}
