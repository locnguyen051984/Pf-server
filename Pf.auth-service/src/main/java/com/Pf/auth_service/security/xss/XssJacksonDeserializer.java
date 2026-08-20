package com.Pf.auth_service.security.xss;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.io.IOException;

public class XssJacksonDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value != null) {
            // Dùng Jsoup để sanitize chuỗi JSON trước khi map vào Object (Entity/DTO)
            return Jsoup.clean(value, Safelist.none());
        }
        return null;
    }
}
