package com.flowbeats.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceManager {
    private static final String PREF_NAME = "FlowBeatsPrefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PASSWORD = "user_password";
    
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    
    public SharedPreferenceManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }
    
    public void saveUserData(String name, String email, String password) {
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_PASSWORD, password);
        editor.apply();
    }
    
    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }
    
    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    public String getUserName() {
        return preferences.getString(KEY_USER_NAME, "");
    }
    
    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, "");
    }
    
    public String getUserPassword() {
        return preferences.getString(KEY_USER_PASSWORD, "");
    }
    
    public void clearUserData() {
        editor.clear();
        editor.apply();
    }
}
