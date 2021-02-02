package com.hbd.starter.access.supervisor.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * 使用baomidou配置多数据源，直接配置在yml文件里面
 * 不需要再写java代码申明dataSource的Bean
 */
//@Configuration
@ConditionalOnProperty(name = "ctl.access.supervisor.enable", havingValue = "true", matchIfMissing = false)
@MapperScan(basePackages = "com.hbd.starter.access.supervisor.mapper", sqlSessionTemplateRef = "accessSqlSessionTemplate")
public class AccessMybatisConfig {

    /**
     * 配置 SqlSessionFactoryBean
     * @ConfigurationProperties 在这里是为了将 MyBatis 的 mapper 位置和持久层接口的别名设置到
     * Bean 的属性中，如果没有使用 *.xml 则可以不用该配置，否则将会产生 invalid bond statement 异常
     * @return the sql session factory bean
     */
    @Bean(name ="accessSqlSessionFactory")
    public SqlSessionFactory sqlSessionFactoryBean(@Qualifier("accessLogDatasource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setTypeAliasesPackage("com.hbd.starter.access.supervisor.entity");
        sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:accessMapper/*.xml"));
        return sqlSessionFactoryBean.getObject();
    }

    @Bean(name = "accessTransactionManager")
    public DataSourceTransactionManager mysqlTransactionManager(@Qualifier("accessLogDatasource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "accessSqlSessionTemplate")
    public SqlSessionTemplate mysqlSqlSessionTemplate(@Qualifier("accessSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
