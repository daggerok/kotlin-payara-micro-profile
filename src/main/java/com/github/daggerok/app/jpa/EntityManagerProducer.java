package com.github.daggerok.app.jpa;

import lombok.extern.log4j.Log4j2;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Log4j2
@ApplicationScoped
public class EntityManagerProducer {

  @Produces
  @Dependent
  @PersistenceContext
  EntityManager entityManager;

  public void close(@Disposes EntityManager entityManager) {
    log.info("bye: {}", entityManager);
    entityManager.close();
  }
}
