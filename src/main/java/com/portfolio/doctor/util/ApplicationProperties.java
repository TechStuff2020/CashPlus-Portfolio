package com.portfolio.doctor.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "api")
@Setter
public class ApplicationProperties {
    private List<String> keys;
    @Getter
    private String tickerUrl;
    private Integer index = 0;

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }
    public String getKey() {
        index = index >= keys.size() ? 0 : index;
        return keys.get(index++);
    }


}
