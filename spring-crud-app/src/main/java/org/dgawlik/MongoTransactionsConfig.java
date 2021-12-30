package org.dgawlik;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;


/**
 * This split is for not to set up mongo cluster during tests.
 */
@Configuration
@Profile("!testing")
@EnableTransactionManagement
public class MongoTransactionsConfig {

    @Bean
    MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {

        return new MongoTransactionManager(dbFactory);
    }
}
