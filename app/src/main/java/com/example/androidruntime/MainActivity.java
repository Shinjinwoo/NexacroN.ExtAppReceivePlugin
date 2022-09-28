package com.example.androidruntime;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.nexacro.NexacroResourceManager;
import com.nexacro.NexacroUpdatorActivity;
import com.tobesoft.plugin.extappreceiveplugin.PreferenceManager;

public class MainActivity extends NexacroUpdatorActivity {
    public MainActivity() {

        super();

        setBootstrapURL("http://smart.tobesoft.co.kr/NexacroN/ExtAppReceivePlugin/_android_/start_android.json");
        setProjectURL("http://smart.tobesoft.co.kr/NexacroN/ExtAppReceivePlugin/_android_/");

        this.setStartupClass(NexacroActivityExt.class);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        NexacroResourceManager.createInstance(this);
        NexacroResourceManager.getInstance().setDirect(true);

        Intent intent = getIntent();
        if (intent != null) {
            String bootstrapURL = intent.getStringExtra("bootstrapURL");
            String projectUrl = intent.getStringExtra("projectUrl");


            Bundle intentData = intent.getExtras();
            if (intentData != null ){
                //PreferenceManager.setString(this,"PMSetKey",intentData.toString());
                PreferenceManager.setBundleToJsonObject(this,"PMSetKey",intentData);
            }


            if (bootstrapURL != null) {
                setBootstrapURL(bootstrapURL);
                setProjectURL(projectUrl);
            }
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
    }
}