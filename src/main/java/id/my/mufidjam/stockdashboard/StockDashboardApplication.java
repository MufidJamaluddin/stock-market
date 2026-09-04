package id.my.mufidjam.stockdashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Reactive Stock Market Dashboard.
 * <p>
 * Stack: Spring WebFlux (reactive IoC container), R2DBC/PostgreSQL, Flyway,
 * Reactive Elasticsearch, Reactive Redis, Reactor Kafka.
 */
@SpringBootApplication
@EnableScheduling
public class StockDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockDashboardApplication.class, args);
    }
}
