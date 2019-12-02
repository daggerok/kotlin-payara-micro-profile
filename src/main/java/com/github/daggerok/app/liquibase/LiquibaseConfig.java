package com.github.daggerok.app.liquibase;

import liquibase.integration.cdi.CDILiquibaseConfig;
import liquibase.integration.cdi.annotations.LiquibaseType;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.sql.DataSource;

@Log4j2
@ApplicationScoped
public class LiquibaseConfig {

  @Resource
  DataSource myDataSource;

  @Produces
  @LiquibaseType
  public CDILiquibaseConfig cdiLiquibaseConfig() {
    log.info("create liquibase CDI config.");
    CDILiquibaseConfig config = new CDILiquibaseConfig();
    config.setChangeLog("liquibase/changelog.xml");
    return config;
  }

  @Produces
  @SneakyThrows
  @LiquibaseType
  public DataSource dataSource() {
    log.info("get liquibase datasource.");
    return myDataSource;
  }

  @Produces
  @LiquibaseType
  public ResourceAccessor resourceAccessor() {
    log.info("get liquibase resource accessor.");
    return new ClassLoaderResourceAccessor(getClass().getClassLoader());
  }
}
