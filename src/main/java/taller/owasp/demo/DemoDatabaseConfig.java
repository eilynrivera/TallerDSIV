
package taller.owasp.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class DemoDatabaseConfig {

    @Bean(name = "demoJdbcTemplate")
    public JdbcTemplate demoJdbcTemplate() {

        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl(
                "jdbc:h2:mem:owasp_demo;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
        );
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        return new JdbcTemplate(dataSource);
    }
}
