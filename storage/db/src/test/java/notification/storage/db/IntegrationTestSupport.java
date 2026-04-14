package notification.storage.db;

import java.time.Instant;
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

@SpringBootTest(classes = IntegrationTestSupport.TestApplication.class)
abstract class IntegrationTestSupport {

    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("notification")
            .withUsername("test")
            .withPassword("test");

    static {
        MYSQL.start();
    }

    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    }

    @Autowired
    protected NotificationRepository notificationRepository;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected EventRepository eventRepository;

    @Autowired
    protected PlatformTransactionManager transactionManager;

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
        eventRepository.deleteAll();
        memberRepository.deleteAll();
    }

    protected NotificationEntity createNotification(String notificationKey) {
        return new NotificationEntity(
                1L,
                100L,
                NotificationType.AFTER_PAID,
                NotificationChanel.EMAIL,
                notificationKey,
                Instant.now()
        );
    }

    protected MemberEntity createMember() {
        return new MemberEntity(
                null,
                "test@test.com",
                "in-app-token",
                true,
                true
        );
    }

    protected EventEntity createEvent() {
        return new EventEntity(
                null,
                "event contents"
        );
    }

    @SpringBootApplication
    static class TestApplication {
    }
}
