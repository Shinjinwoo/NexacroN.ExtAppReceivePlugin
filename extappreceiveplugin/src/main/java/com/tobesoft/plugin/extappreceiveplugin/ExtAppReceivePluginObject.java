package com.tobesoft.plugin.extappreceiveplugin;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.nexacro.NexacroActivity;
import com.nexacro.plugin.NexacroPlugin;
import com.tobesoft.plugin.extappreceiveplugin.plugininterface.ExtAppReceivePluginInterface;

import org.json.JSONObject;

public class ExtAppReceivePluginObject extends NexacroPlugin {

    private static final String SVCID = "svcid";
    private static final String REASON = "reason";
    private static final String RETVAL = "returnvalue";

    private static final String CALL_BACK = "_oncallback";

    private static final String METHOD_CALLMETHOD = "callMethod";

    public static final int CODE_RECEIVE = 1;
    public static final int CODE_SUCCESS = 0;
    public static final int CODE_ERROR = -1;

    private final static String LOG_TAG = "ExtAppReceivePlugin";


    public String mServiceId = "";


    Activity mActivity;
    ExtAppReceivePluginInterface mExtAppReceivePluginInterface;
    private ExtAppReceivePluginObject mExtAppReceivePluginObject;

    public ExtAppReceivePluginObject(String objectId) {
        super(objectId);

        mExtAppReceivePluginInterface = (ExtAppReceivePluginInterface) NexacroActivity.getInstance();
        mExtAppReceivePluginInterface.setExtAppReceivePluginObject(this);
        mActivity = (Activity) NexacroActivity.getInstance();

        mExtAppReceivePluginObject = this;
    }

    @Override
    public void init(JSONObject paramObject) {

    }

    @Override
    public void release(JSONObject paramObject) {

    }

    @Override
    public void execute(String method, JSONObject paramObject) {

        mServiceId = "";
        if (method.equals(METHOD_CALLMETHOD)) {

            try {
                JSONObject params = paramObject.getJSONObject("params");
                mServiceId = params.getString("serviceid");
                if (mServiceId.equals("checkReceive")) {

                    String receivedData = PreferenceManager.getString(mActivity,"PMSetKey");
                    PreferenceManager.clear(mActivity);

                    send(CODE_SUCCESS,receivedData);

//                    Intent intent = mActivity.getPackageManager().getLaunchIntentForPackage("com.android.chrome");
//                    mActivity.startActivity(intent);
                }
            } catch (Exception e) {
                send(CODE_ERROR,e);
            }
        }
    }

    public boolean send(int reason, Object retval) {
        return send(mServiceId, CALL_BACK, reason, retval);
    }

    public boolean send(String svcid, String callMethod, int reason, Object retval) {

        JSONObject obj = new JSONObject();

        try {
            if (mServiceId != null) {
                obj.put(SVCID, svcid);
                obj.put(REASON, reason);
                obj.put(RETVAL, retval);

                callback(callMethod, obj);
            }

        } catch (Exception e) {
            e.printStackTrace();

        }

        return false;
    }


    public void onResume() {
        String receivedData = PreferenceManager.getString(mActivity,"PMSetKey");
        Log.e(LOG_TAG, "onResume: " + receivedData );
        PreferenceManager.clear(mActivity);

        send(CODE_SUCCESS,receivedData);
    }
}
