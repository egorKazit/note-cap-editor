package com.yk.capper.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ObjectConvertorTypes {
    @Bean
    @Primary
    public ObjectMapper primaryObjectMapper(){
        ObjectMapper o = new ObjectMapper();
        o.registerModule(new JavaTimeModule());
        return o;
    }
}
