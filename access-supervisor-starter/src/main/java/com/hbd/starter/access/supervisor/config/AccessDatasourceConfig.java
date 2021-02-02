package com.hbd.starter.access.supervisor.config;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
@ConditionalOnProperty(name = "ctl.access.supervisor.enable", havingValue = "true", matchIfMissing = false)
@Slf4j
public class AccessDatasourceConfig {

    @Value("${ctl.access.supervisor.db.url}")
    private String dbUrl;

    @Value("${ctl.access.supervisor.db.username}")
    private String username;

    @Value("${ctl.access.supervisor.db.password}")
    private String password;

    @Value("${ctl.access.supervisor.db.driver-class-name}")
    private String driverClassName;

    @Value("${ctl.access.supervisor.db.initialSize}")
    private int initialSize;

    @Value("${ctl.access.supervisor.db.minIdle}")
    private int minIdle;

    @Value("${ctl.access.supervisor.db.maxActive}")
    private int maxActive;

    @Value("${ctl.access.supervisor.db.maxWait}")
    private int maxWait;

    @Bean(name = "accessLogDatasource")
    public DataSource druidDataSource() throws SQLException {
        DruidDataSource datasource = new DruidDataSource();
        datasource.setUrl(this.dbUrl);
        datasource.setUsername(username);
        datasource.setPassword(password);
        datasource.setDriverClassName(driverClassName);
        datasource.setInitialSize(initialSize);
        datasource.setMinIdle(minIdle);
        datasource.setMaxActive(maxActive);
        datasource.setMaxWait(maxWait);
        datasource.setTimeBetweenEvictionRunsMillis(60000);
        datasource.setMinEvictableIdleTimeMillis(300000);
        datasource.setValidationQuery("select 'x'");
        datasource.setTestWhileIdle(true);
        datasource.setTestOnBorrow(false);
        datasource.setTestOnReturn(false);
        datasource.setPoolPreparedStatements(true);
        return datasource;
    }

}