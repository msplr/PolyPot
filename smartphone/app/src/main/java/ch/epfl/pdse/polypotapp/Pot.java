package ch.epfl.pdse.polypotapp;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

class Pot {
    public final String name;
    public final String server;
    public final String uuid;

    Pot(String name, String server, String uuid) {
        this.name = name;
        this.server = server;
        this.uuid = uuid;
    }

    public static String getPreferenceName(String server, String uuid) {
        return server.split("/")[2] + "-" + uuid;
    }

    public static ArrayList<Pot> getPots(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String potsString = sharedPreferences.getString("list", "[{\"name\":\"Default pot\",\"server\":\"https://polypot.0xf00.ch\",\"uuid\":\"01234567-89ab-cdef-0123-456789abcdef\"}]");
;
        return new Gson().fromJson(potsString, new TypeToken<ArrayList<Pot>>(){}.getType());
    }

    public static void savePots(Context context, ArrayList<Pot> pots) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("list", new Gson().toJson(pots));
        editor.apply();
    }

    public static boolean exists(ArrayList<Pot> pots, String server, String uuid) {
        boolean found = false;
        for(Pot pot : pots) {
            if(pot.server.equals(server) && pot.uuid.equals(uuid)) {
                found = true;
            }
        }
        return found;
    }
}
