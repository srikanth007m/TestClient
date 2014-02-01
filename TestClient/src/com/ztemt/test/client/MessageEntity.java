package com.ztemt.test.client;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class MessageEntity {

    private static final String TAG = "MessageEntity";

    private static final String NAME_ID   = "msgId";
    private static final String NAME_TYPE = "type";

    private JSONObject mJson;

    public MessageEntity() {
        mJson = new JSONObject();
    }

    public MessageEntity(int type) {
        mJson = new JSONObject();
        putInt(NAME_TYPE, type);
    }

    public MessageEntity(String json) {
        try {
            mJson = new JSONObject(json);
        } catch (JSONException e) {
            mJson = new JSONObject();
        }
    }

    public MessageEntity(JSONObject json) {
        if (json != null) {
            mJson = json;
        } else {
            mJson = new JSONObject();
        }
    }

    public int getId() {
        return getInt(NAME_ID, 0);
    }

    public void setId(int id) {
        putInt(NAME_ID, id);
    }

    public int getType() {
        return getInt(NAME_TYPE, 0);
    }

    public void setType(int type) {
        putInt(NAME_TYPE, type);
    }

    public int getInt(String name, int defValue) {
        return mJson.optInt(name, defValue);
    }

    public void putInt(String name, int value) {
        try {
            mJson.put(name, value);
        } catch (JSONException e) {
            Log.e(TAG, "putInt", e);
        }
    }

    public long getLong(String name, long defValue) {
        return mJson.optLong(name, defValue);
    }

    public void putLong(String name, long value) {
        try {
            mJson.put(name, value);
        } catch (JSONException e) {
            Log.e(TAG, "putLong", e);
        }
    }

    public String getString(String name, String defValue) {
        return mJson.optString(name, defValue);
    }

    public void putString(String name, String value) {
        try {
            mJson.put(name, value);
        } catch (JSONException e) {
            Log.e(TAG, "putString", e);
        }
    }

    public double getDouble(String name, double defValue) {
        return mJson.optDouble(name, defValue);
    }

    public void putBoolean(String name, boolean value) {
        try {
            mJson.put(name, value);
        } catch (JSONException e) {
            Log.e(TAG, "putBoolean", e);
        }
    }

    public boolean getBoolean(String name, boolean defValue) {
        return mJson.optBoolean(name, defValue);
    }

    public void putArray(String name, List<MessageEntity> entities) {
        try {
            mJson.put(name, new JSONArray(entities));
        } catch (JSONException e) {
            Log.e(TAG, "putArrary", e);
        }
    }

    public List<MessageEntity> getArray(String name) {
        return getArray(name, false);
    }

    public List<MessageEntity> getArray(String name, boolean appendId) {
        List<MessageEntity> entities = new ArrayList<MessageEntity>();
        JSONArray array = null;

        try {
            array = new JSONArray(mJson.optString(name, "[]"));
        } catch (JSONException e) {
            Log.e(TAG, "getArray", e);
        }

        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                MessageEntity entity = new MessageEntity(array.optJSONObject(i));
                if (appendId) entity.setId(getId());
                entities.add(entity);
            }
        }

        return entities;
    }

    @Override
    public String toString() {
        return mJson.toString();
    }
}
