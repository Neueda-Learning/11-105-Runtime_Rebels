package com.portfoliomanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Portfolio Manager backend REST API.
 *
 * Built with Spring Boot + Spring JDBC (JdbcTemplate) + MySQL.
 * No JPA/Hibernate is used - all persistence is done with plain SQL via JdbcTemplate,
 * with Flyway managing schema migrations so the data model can evolve safely as the
 * customer's requirements change (agile delivery).
 */
@SpringBootApplication
@EnableScheduling
public class PortfolioManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortfolioManagerApplication.class, args);
    }
}
