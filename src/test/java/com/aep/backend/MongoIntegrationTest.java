package com.aep.backend;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
public abstract class MongoIntegrationTest {

    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7").withReplicaSet();

    static {
        if (DockerClientFactory.instance().isDockerAvailable()) {
            MONGO.start();
        }
    }

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri", () -> MONGO.getReplicaSetUrl("aepdb"));
    }
}
