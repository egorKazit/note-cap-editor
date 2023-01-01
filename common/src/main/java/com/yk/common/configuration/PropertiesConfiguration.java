package com.yk.common.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.annotation.PostConstruct;

@Configuration
@Getter
@Setter
//@PropertySource("classpath:application.properties")
@Order(1)
@Log4j2
public class PropertiesConfiguration {
    @Value("${HOLDER_PATH}")
    private String service;

    @PostConstruct
    public void checkServ(){
       log.atWarn().log("Service is set as {}",service);
       service = StringUtils.removeEnd(service, "/");
    }

}
