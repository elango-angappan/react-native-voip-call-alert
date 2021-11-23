package com.ajith.voipcall;


import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;
import java.util.Map;

@ReactModule(name = "VoipListener")
public class VoipModule extends ReactContextBaseJavaModule {

    private ReactContext reactContext;

    private static DeviceEventManagerModule.RCTDeviceEventEmitter eventEmitter = null;

    private RNOperationCallback rnOperationCallback;

    public interface RNOperationCallback {
        public void onSuccess(String message);
        public void onFailure(String message);
    }

    public VoipModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public void initialize() {
        super.initialize();
        eventEmitter = reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
    }

    /**
     * @return the name of this module. This will be the name used to {@code require()} this module
     * from JavaScript.
     */
    @Override
    public String getName() {
        return "VoipListener";
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("MyEventName", "MyEventValue");
        return constants;
    }

    @ReactMethod
    public void successCallback(String message) {
        if (rnOperationCallback != null) {
            rnOperationCallback.onSuccess(message);
        }
    }

    @ReactMethod
    public void failureCallback(String message) {
        if (rnOperationCallback != null) {
            rnOperationCallback.onFailure(message);
        }
    }

    public void setRnOperationCallback(RNOperationCallback rnOperationCallback) {
        this.rnOperationCallback = rnOperationCallback;
    }

}
