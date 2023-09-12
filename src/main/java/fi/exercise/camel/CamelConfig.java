package fi.exercise.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelConfig {

    @Value("${auto.startup}")
    private Boolean startup;

    @Bean
    CamelContextConfiguration camelContextConfiguration() {
        return new CamelContextConfiguration() {
            @Override
            public void beforeApplicationStart(CamelContext camelContext) {
                camelContext.setAutoStartup(startup);
                camelContext.disableJMX();
            }

            @Override
            public void afterApplicationStart(CamelContext camelContext) {
                System.out.println("Context started :: " + camelContext.getName());
            }
        };
    }
    
}
