package com.example.gunluk.utils;

import android.content.Context;

import com.example.gunluk.models.UserModel;

/**
 * Created by sesumy on 17.12.2016.
 */

public class PrefUtils {
    public static void setCurrentUser(UserModel currentUser, Context ctx){
        ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(ctx, "user_prefs", 0);
        complexPreferences.putObject("current_user_value", currentUser);
        complexPreferences.commit();
    }

    public static UserModel getCurrentUser(Context ctx){
        ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(ctx, "user_prefs", 0);
        UserModel currentUser = complexPreferences.getObject("current_user_value", UserModel.class);
        return currentUser;
    }

    public static void clearCurrentUser( Context ctx){
        ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(ctx, "user_prefs", 0);
        complexPreferences.clearObject();
        complexPreferences.commit();
    }

}
