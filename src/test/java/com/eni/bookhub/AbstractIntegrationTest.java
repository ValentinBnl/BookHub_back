package com.eni.bookhub;

import com.eni.bookhub.service.BookCoverSchedulerService;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.utility.DockerImageName;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
public abstract class AbstractIntegrationTest {

    // Le scheduler est mocké pour éviter les appels HTTP vers OpenLibrary pendant les tests
    @MockitoBean
    BookCoverSchedulerService bookCoverSchedulerService;

    /*
     * Singleton container pattern : le conteneur est démarré une seule fois pour toute la JVM,
     * pas par classe de test. Testcontainers enregistre automatiquement un shutdown hook (via Ryuk)
     * pour l'arrêter en fin d'exécution.
     * Sans ce pattern, le conteneur s'arrête et redémarre entre chaque classe, changeant de port
     * alors que le contexte Spring a mis en cache l'ancienne URL.
     */
    static final MSSQLServerContainer<?> SQL_SERVER =
            new MSSQLServerContainer<>(DockerImageName.parse("mcr.microsoft.com/mssql/server:2022-latest"))
                    .acceptLicense();

    static {
        SQL_SERVER.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", SQL_SERVER::getJdbcUrl);
        registry.add("spring.datasource.username", SQL_SERVER::getUsername);
        registry.add("spring.datasource.password", SQL_SERVER::getPassword);
        // Hibernate crée le schéma au démarrage et le supprime à la fin
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("jwt.secret", () -> "integration-test-jwt-secret-32-chars");
    }
}
