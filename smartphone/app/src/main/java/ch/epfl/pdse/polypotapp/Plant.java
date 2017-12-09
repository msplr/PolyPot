package ch.epfl.pdse.polypotapp;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Plant {
    public final String name;
    public final String description;

    public final float waterLevelMin;
    public final float waterLevelMax;
    public final String waterLevelDescription;

    public final float temperatureMin;
    public final float temperatureMax;
    public final String temperatureDescription;

    public final float soilMoistureMin;
    public final float soilMoistureMax;
    public final String soilMoistureDescription;

    public final float luminosityMin;
    public final float luminosityMax;
    public final String luminosityDescription;

    public static LinkedTreeMap<String, Object> getPlantsList(Context context) {
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.plants);

            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }
            String jsonString = total.toString();

            return new Gson().fromJson(jsonString, new TypeToken<LinkedTreeMap<String, Object>>() {}.getType());
        } catch (IOException e) {
            return null;
        }
    }

    public Plant(LinkedTreeMap<String, Object> plantsList, String specie) {
        LinkedTreeMap<String, Object> plant = (LinkedTreeMap<String, Object>) plantsList.get(specie);

        name = specie;
        description = (String) plant.get("description");

        String[] waterLevelRange = ((String) plant.get("waterLevelRange")).split("/");
        waterLevelMin = Float.valueOf(waterLevelRange[0]);
        waterLevelMax = Float.valueOf(waterLevelRange[1]);
        waterLevelDescription = (String) plant.get("waterLevelDescription");

        String[] temperatureRange = ((String) plant.get("temperatureRange")).split("/");
        temperatureMin = Float.valueOf(temperatureRange[0]);
        temperatureMax = Float.valueOf(temperatureRange[1]);
        temperatureDescription = (String) plant.get("temperatureDescription");

        String[] soilMoistureRange = ((String) plant.get("soilMoistureRange")).split("/");
        soilMoistureMin = Float.valueOf(soilMoistureRange[0]);
        soilMoistureMax = Float.valueOf(soilMoistureRange[1]);
        soilMoistureDescription = (String) plant.get("soilMoistureDescription");

        String[] luminosityRange = ((String) plant.get("luminosityRange")).split("/");
        luminosityMin = Float.valueOf(luminosityRange[0]);
        luminosityMax = Float.valueOf(luminosityRange[1]);
        luminosityDescription = (String) plant.get("luminosityDescription");
    }
}
