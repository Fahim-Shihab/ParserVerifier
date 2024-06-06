package finance.gov.bd.csvParser.common;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class BaseModel {
    static Gson gson = null;

    public BaseModel() {
        buildGsonInstance();
    }

    public static Gson getGson() {
        if (gson == null) {
            buildGsonInstance();
        }
        return gson;
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }

    public String toJsonString() {
        try {
            String toJson = getGson().toJson(this);
            return toJson;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return getGson().fromJson(json, classOfT);
    }

    private static void buildGsonInstance() {
        if (gson == null) {

            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.serializeNulls();

            gson = gsonBuilder.setDateFormat("dd-MM-yyyy").setPrettyPrinting().disableHtmlEscaping().create();
        }
    }
}
