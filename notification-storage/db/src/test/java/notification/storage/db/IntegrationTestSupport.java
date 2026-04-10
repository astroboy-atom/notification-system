package notification.storage.db;

import notification.enums.NotificationChanel;
import notification.enums.NotificationType;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(classes = IntegrationTestSupport.TestApplication.class)
abstract class IntegrationTestSupport {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.40")
            .withDatabaseName("notification")
            .withUsername("root")
            .withPassword("root");

    @DynamicPropertySource
    public static void overrideProperty(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        dynamicPropertyRegistry.add("spring.datasource.username", () -> "root");
        dynamicPropertyRegistry.add("spring.datasource.password", () -> "root");
        dynamicPropertyRegistry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    }

    @Autowired
    protected NotificationRepository notificationRepository;

    @Autowired
    protected PlatformTransactionManager transactionManager;

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
    }

    protected NotificationEntity createNotification(String notificationKey) {
        return new NotificationEntity(
                1L,
                100L,
                NotificationType.AFTER_PAID,
                NotificationChanel.EMAIL,
                notificationKey
        );
    }

    @SpringBootApplication
    static class TestApplication {
    }
}
