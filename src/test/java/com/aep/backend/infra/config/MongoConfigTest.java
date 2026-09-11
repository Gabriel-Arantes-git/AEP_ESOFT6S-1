package com.aep.backend.infra.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class MongoConfigTest {

    @Test
    @DisplayName("Deve criar o gerenciador de transações do Mongo")
    void deveCriarGerenciadorDeTransacoesDoMongo() {
        MongoConfig mongoConfig = new MongoConfig();
        MongoDatabaseFactory dbFactory = mock(MongoDatabaseFactory.class);

        MongoTransactionManager transactionManager = mongoConfig.transactionManager(dbFactory);

        assertNotNull(transactionManager);
    }
}
