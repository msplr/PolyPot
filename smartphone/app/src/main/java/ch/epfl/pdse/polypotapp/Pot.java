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
    public final String color;

    Pot(String name, String server, String uuid, String color) {
        this.name = name;
        this.server = server;
        this.uuid = uuid;
        this.color = color;
    }

    public static String getPreferenceName(String server, String uuid) {
        return server.split("/")[2] + "-" + uuid;
    }

    public static ArrayList<Pot> getPots(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String potsString = sharedPreferences.getString(
                "list",
                "[" +
                        "{\"name\":\"Default pot\",\"server\":\"https://polypot.0xf00.ch\",\"uuid\":\"01234567-89ab-cdef-0123-456789abcdef\",\"color\":\"#cddc39\"}," +
                        "{\"name\":\"Empty pot\",\"server\":\"https://polypot.0xf00.ch\",\"uuid\":\"00000000-0000-0000-0000-000000000000\",\"color\":\"#00bcd4\"}," +
                        "{\"name\":\"Bad pot\",\"server\":\"https://polypot.0xf00.ch\",\"uuid\":\"11111111-1111-1111-1111-111111111111\",\"color\":\"#f44336\"}" +
                    "]");

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
