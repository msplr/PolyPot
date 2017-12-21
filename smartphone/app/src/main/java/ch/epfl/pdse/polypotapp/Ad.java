package ch.epfl.pdse.polypotapp;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class Ad {
    private static ArrayList<Object> mAdsList;
    private static Random mRandom;

    public final String text;
    public final String url;

    public static boolean getAdsList(InputStream inputStream) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }
            String jsonString = total.toString();

            mAdsList = new Gson().fromJson(jsonString, new TypeToken<ArrayList<Object>>() {}.getType());
            mRandom = new Random();

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public Ad() {
        int i = mRandom.nextInt(mAdsList.size());

        LinkedTreeMap<String, String> ad = (LinkedTreeMap<String, String>) mAdsList.get(i);

        text = ad.get("text");
        url = ad.get("url");
    }
}
