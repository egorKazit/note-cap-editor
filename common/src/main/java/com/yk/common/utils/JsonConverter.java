package com.yk.common.utils;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonConverter {

    private final Gson gsonConverter = new Gson();

    public <T> T convertFromJson(String json, Class<T> classOfT) {
        return gsonConverter.fromJson(json, classOfT);
    }

    public String convertToJson(Object src) {
        return gsonConverter.toJson(src);
    }

    public enum InstanceHolder {
        instance;
        private final JsonConverter jsonConverter = new JsonConverter();

        public final JsonConverter getInstance() {
            return jsonConverter;
        }
    }

}
