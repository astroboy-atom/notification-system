package notification.publish;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "notification")
@EntityScan(basePackages = "notification.storage.db")
@EnableJpaRepositories(basePackages = "notification.storage.db")
@EnableScheduling
public class NotificationPublishApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationPublishApplication.class, args);
    }
}
