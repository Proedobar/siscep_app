package com.proedobar.siscep.session;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.proedobar.siscep.models.LoginResponse;

public class SessionManager {
    private static final String PREF_NAME = "SiscepSession";
    private static final String KEY_USER_DATA = "user_data";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private static SessionManager instance;
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private final Gson gson;
    private LoginResponse.UserData userData;

    private SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
        gson = new Gson();
        loadUserData();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    public void saveUserSession(LoginResponse loginResponse) {
        if (loginResponse != null && loginResponse.getData() != null) {
            editor.putString(KEY_USER_DATA, gson.toJson(loginResponse.getData()));
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.apply();
            userData = loginResponse.getData();
        }
    }

    private void loadUserData() {
        String userJson = prefs.getString(KEY_USER_DATA, null);
        if (userJson != null) {
            userData = gson.fromJson(userJson, LoginResponse.UserData.class);
        }
    }

    public LoginResponse.UserData getUserData() {
        return userData;
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logout() {
        editor.clear();
        editor.apply();
        userData = null;
    }

    // Métodos de conveniencia para acceder a datos específicos
    public int getUserId() {
        return userData != null ? userData.getUserId() : -1;
    }

    public String getNombre() {
        return userData != null ? userData.getNombre() : "";
    }

    public String getCedula() {
        return userData != null ? userData.getCedula() : "";
    }

    public String getFechaIngreso() {
        return userData != null ? userData.getFechaIngreso() : "";
    }

    public String getCargo() {
        return userData != null ? userData.getCargo() : "";
    }

    public String getTipoCargo() {
        return userData != null ? userData.getTipoCargo() : "";
    }
} 