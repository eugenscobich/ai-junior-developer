package ai.junior.developer.controller;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

public class TestUtilities {
    public static byte[] convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsBytes(object);
    }

    public static MediaType APPLICATION_JSON_UTF8 = MediaType.APPLICATION_JSON;
}