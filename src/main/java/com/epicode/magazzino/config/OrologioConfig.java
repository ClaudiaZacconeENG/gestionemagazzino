package com.epicode.magazzino.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** L'ora "di adesso" arriva da un bean: nei test si può sostituire con un orologio fisso. */
@Configuration
public class OrologioConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
