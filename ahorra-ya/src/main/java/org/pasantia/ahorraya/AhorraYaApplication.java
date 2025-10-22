package org.pasantia.ahorraya;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("org.pasantia.ahorraya.model")
@EnableJpaRepositories("org.pasantia.ahorraya.repository")

public class AhorraYaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AhorraYaApplication.class, args);
    }

}
