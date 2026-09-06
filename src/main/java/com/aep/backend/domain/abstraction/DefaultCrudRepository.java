package com.aep.backend.domain.abstraction;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface DefaultCrudRepository<E extends DefaultEntity> extends MongoRepository<E, String> {
}
