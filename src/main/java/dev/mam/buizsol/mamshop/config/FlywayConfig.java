package dev.mam.buizsol.mamshop.config;

import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true", matchIfMissing = true)
public class FlywayConfig {

    @Bean
    public Flyway flyway(DataSource dataSource) {
        log.info("Manually initializing Flyway...");
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .schemas("public")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .cleanDisabled(false)
                .load();

        log.info("Starting Flyway migration...");
        try {
            var result = flyway.migrate();
            log.info("Flyway migration completed successfully: {}", result.success);
        } catch (Exception e) {
            log.error("Flyway migration failed!", e);
        }
        return flyway;
    }
}
