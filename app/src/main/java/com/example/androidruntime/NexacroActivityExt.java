package com.example.androidruntime;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


import com.nexacro.NexacroActivity;
import com.tobesoft.plugin.extappreceiveplugin.ExtAppReceivePluginObject;
import com.tobesoft.plugin.extappreceiveplugin.PreferenceManager;
import com.tobesoft.plugin.extappreceiveplugin.plugininterface.ExtAppReceivePluginInterface;


public class NexacroActivityExt extends NexacroActivity implements ExtAppReceivePluginInterface {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {

        if ( mExtAppReceivePluginObject != null) {
            mExtAppReceivePluginObject.onResume();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    ExtAppReceivePluginObject mExtAppReceivePluginObject;
    @Override
    public void setExtAppReceivePluginObject(ExtAppReceivePluginObject extAppReceivePluginObject) {
        this.mExtAppReceivePluginObject = extAppReceivePluginObject;
    }
}
