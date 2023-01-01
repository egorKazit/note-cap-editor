package com.yk.capper;

import com.yk.common.configuration.PropertiesConfiguration;
import com.yk.common.service.OAuthProviderImp;
import com.yk.common.service.PasswordRetrieverImp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(basePackages = {"com.yk.common", "com.yk.capper"}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = PropertiesConfiguration.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = PasswordRetrieverImp.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = OAuthProviderImp.class)
})
public class CapperApplication {

    public static void main(String[] args) {
        SpringApplication.run(CapperApplication.class, args);
    }

}
