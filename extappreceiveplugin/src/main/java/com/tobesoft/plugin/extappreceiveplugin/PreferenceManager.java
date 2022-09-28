package com.tobesoft.plugin.extappreceiveplugin;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.kh.plugin.plugincommonlib.util.ImageUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


public class PreferenceManager {

    public static final String PREFERENCES_NAME = "rebuild_preference";
    private static final String DEFAULT_VALUE_STRING = "";
    private static final boolean DEFAULT_VALUE_BOOLEAN = false;
    private static final int DEFAULT_VALUE_INT = -1;
    private static final long DEFAULT_VALUE_LONG = -1L;
    private static final float DEFAULT_VALUE_FLOAT = -1F;


    public static final String LOG_TAG = "PreferenceManager";


    private static Context mContext = null;
    private static int mResizeScale = 0;



    /**
     * Json 형태로 값 저장.
     *
     * @param context
     * @param key
     * @param intent  Intent를 모듈단에서 가공가능 하도록 Json 형태로 받는 메소드 입니다.
     *                이미지의 Uri -> 비트맵 -> Base64 처리를 RxJava를 통해 IO 쓰레드에서 처리 함으로써
     *                비동기처리를 진행합니다.
     */
    @SuppressLint("LongLogTag")
    public static void setIntentToJson(Context context, String key, Intent intent, int resizeScale) {

        mContext = context;
        mResizeScale = resizeScale;

        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        String action = intent.getAction();
        String type = intent.getType();
        String someValue = "";

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                someValue = intent.getStringExtra(Intent.EXTRA_TEXT);

            } else if (type.startsWith("image/")) {
                someValue = intent.getParcelableExtra(Intent.EXTRA_STREAM).toString();
                someValue = handleSendImage(someValue);
                Log.e(TAG, "setIntentToJson someText: " + someValue);
            } else if (type.startsWith("video")) {
                someValue = intent.getParcelableExtra(Intent.EXTRA_STREAM).toString();
                Log.e(TAG, "setIntentToJson someText: " + someValue);
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            someValue = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM).toString();
            JSONObject jsonObject = handleSendMultipleImages(someValue);
            someValue = String.valueOf(jsonObject);
            Log.e(TAG, someValue);
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("action", action);
            jsonObject.put("type", type);
            jsonObject.put("value", someValue);
        } catch (JSONException e2) {
            e2.printStackTrace();
        }

        editor.putString(key, String.valueOf(jsonObject));
        editor.commit();

        Log.d("PreferenceManager", "setIntentToJson: " + someValue);
    }


    public static String handleSendImage(String value) {
        Uri imageUri = Uri.parse(value);
        if (imageUri != null) {
            try {
                InputStream inputStream = mContext.getApplicationContext().getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                String sharedImage = bitmapToBase64(bitmap);
                return sharedImage;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }


    public static JSONObject handleSendMultipleImages(String value) {

        ArrayList<Uri> someMultipleImageUris = new ArrayList<>();
        String optimizationString = value.trim().replace("[", "").replace("]", "").replace(" ", "");
        List<String> someMultipleImageUrisToString = new ArrayList<>(Arrays.asList(optimizationString.split(",")));
        Log.e(LOG_TAG, String.valueOf(someMultipleImageUrisToString));

        JSONObject jsonObject = new JSONObject();
        for (int i = 0; i < someMultipleImageUrisToString.size(); i++) {
            someMultipleImageUris.add(Uri.parse(someMultipleImageUrisToString.get(i)));
        }

        Log.d(LOG_TAG, String.valueOf(someMultipleImageUris));
        try {
            for (int i = 0; i < someMultipleImageUris.size(); i++) {
                InputStream inputStream = mContext.getContentResolver().openInputStream(someMultipleImageUris.get(i));
                Bitmap bitmaps = BitmapFactory.decodeStream(inputStream);
                jsonObject.put("imageItem" + i, bitmapToBase64(bitmaps));
            }

            Log.e(LOG_TAG, String.valueOf(jsonObject));
            return jsonObject;

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String bitmapToBase64(Bitmap bitmap) {

        ImageUtil imageUtil = ImageUtil.getInstance();
        Bitmap resizeBitmap = imageUtil.resizeBitmap(bitmap, mResizeScale);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        resizeBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

        byte[] byteArray = byteArrayOutputStream.toByteArray();
        try {
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    /**
     *
     * Bundle to String 값
     * @param context
     * @param key
     * @param bundle
     */

    public static void setBundleToString(Context context, String key, Bundle bundle) {
        PreferenceManager.setString(context,key,bundle.toString());
    }

    /**
     *Bundle to JsonObject 값
     */

    public static void setBundleToJsonObject(Context context, String prfeKey, Bundle bundle) {
        JSONObject json = new JSONObject();
        Set<String> keys = bundle.keySet();
        for (String key : keys) {
            try {
                // json.put(key, bundle.get(key)); see edit below
                json.put(key, JSONObject.wrap(bundle.get(key)));
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
        PreferenceManager.setString(context,prfeKey,json.toString());
    }


    /**
     * String 값 저장
     *
     * @param context
     * @param key
     * @param value
     */

    public static void setString(Context context, String key, String value) {

        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        Log.d("PreferenceManager", "setString: " + value);

        editor.putString(key, value);
        editor.commit();
    }

    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    /**
     * boolean 값 저장
     *
     * @param context
     * @param key
     * @param value
     */

    public static void setBoolean(Context context, String key, boolean value) {
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.commit();

    }


    /**
     * int 값 저장
     *
     * @param context
     * @param key
     * @param value
     */

    public static void setInt(Context context, String key, int value) {

        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(key, value);
        editor.commit();

    }


    /**
     * long 값 저장
     *
     * @param context
     * @param key
     * @param value
     */

    public static void setLong(Context context, String key, long value) {

        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong(key, value);
        editor.commit();

    }


    /**
     * float 값 저장
     *
     * @param context
     * @param key
     * @param value
     */

    public static void setFloat(Context context, String key, float value) {

        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putFloat(key, value);
        editor.commit();

    }


    /**
     * String 값 로드
     *
     * @param context
     * @param key
     * @return
     */

    public static String getString(Context context, String key) {
        SharedPreferences prefs = getPreferences(context);

        String value = prefs.getString(key, DEFAULT_VALUE_STRING);
        Log.d("PreferenceManager", "getString: " + value);


        return value;
    }


    /**
     * boolean 값 로드
     *
     * @param context
     * @param key
     * @return
     */

    public static boolean getBoolean(Context context, String key) {

        SharedPreferences prefs = getPreferences(context);
        boolean value = prefs.getBoolean(key, DEFAULT_VALUE_BOOLEAN);

        return value;

    }


    /**
     * int 값 로드
     *
     * @param context
     * @param key
     * @return
     */

    public static int getInt(Context context, String key) {

        SharedPreferences prefs = getPreferences(context);
        int value = prefs.getInt(key, DEFAULT_VALUE_INT);

        return value;

    }


    /**
     * long 값 로드
     *
     * @param context
     * @param key
     * @return
     */

    public static long getLong(Context context, String key) {

        SharedPreferences prefs = getPreferences(context);
        long value = prefs.getLong(key, DEFAULT_VALUE_LONG);

        return value;

    }


    /**
     * float 값 로드
     *
     * @param context
     * @param key
     * @return
     */

    public static float getFloat(Context context, String key) {

        SharedPreferences prefs = getPreferences(context);
        float value = prefs.getFloat(key, DEFAULT_VALUE_FLOAT);

        return value;

    }


    /**
     * 키 값 삭제
     *
     * @param context
     * @param key
     */

    public static void removeKey(Context context, String key) {

        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();

        edit.remove(key);
        edit.commit();

    }


    /**
     * 모든 저장 데이터 삭제
     *
     * @param context
     */

    public static void clear(Context context) {

        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();

        edit.clear();
        edit.commit();
    }

}

