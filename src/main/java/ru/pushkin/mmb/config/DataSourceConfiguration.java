package ru.pushkin.mmb.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "mmbEntityManager",
        transactionManagerRef = "mmbTransactionManager",
        basePackages = "ru.pushkin.mmb.data.repository"
)
public class DataSourceConfiguration {

    @Primary
    @Bean(name = "mmbEntityManager")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(mmbDataSource())
                .packages("ru.pushkin.mmb.data.model")
                .persistenceUnit("mmbPU")
                .build();
    }

    @Bean
    @Primary
    public DataSource mmbDataSource() {
        return mmbDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    @Primary
    @ConfigurationProperties("mmb.datasource")
    public DataSourceProperties mmbDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean(name = "mmbTransactionManager")
    public PlatformTransactionManager mmbTransactionManager(@Qualifier("mmbEntityManager") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
