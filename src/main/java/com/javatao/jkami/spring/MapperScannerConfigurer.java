package com.javatao.jkami.spring;

import static org.springframework.util.Assert.notNull;

import java.lang.annotation.Annotation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

import com.javatao.jkami.RunConfing;
import com.javatao.jkami.annotations.KaMiDao;
import com.javatao.jkami.proxy.MapperProxy;

/**
 * 扫描注册类
 * 
 * @author TLF
 */
public class MapperScannerConfigurer implements BeanDefinitionRegistryPostProcessor, InitializingBean {
    /**
     * 扫描包
     */
    private String basePackage;
    
    /**
     * 扫描注解dao
     */
    private Class<? extends Annotation> annotation = KaMiDao.class;
    /**
     * 配置参数
     */
    private RunConfing confing = new RunConfing();

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public void setDbType(String dbType) {
        confing.setDbType(dbType);
    }

    public void setDataSourceId(String dataSourceId) {
        confing.setDataSourceId(dataSourceId);
    }

    public void setLazybean(boolean lazybean) {
        confing.setLazybean(lazybean);
    }

    public void setSqlpath(String sqlpath) {
        confing.setSqlpath(sqlpath);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {}

    @Override
    public void afterPropertiesSet() throws Exception {
        notNull(this.basePackage, "Property 'basePackage' is required");
        
        
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        /**
         * 注册通用Dao
         */
        registerBaseCommonDao(registry);
        /**
         * 注册代理类
         */
        registerProxyHandler(registry);
        /**
         * 加载其他层接口
         */
        ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry, annotation);
        scanner.scan(StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
    }

    private void registerProxyHandler(BeanDefinitionRegistry registry) {
        GenericBeanDefinition mapperProxyDefinition = new GenericBeanDefinition();
        mapperProxyDefinition.setBeanClass(MapperProxy.class);
        mapperProxyDefinition.getPropertyValues().add("confing", confing);
        registry.registerBeanDefinition("mapperProxy", mapperProxyDefinition);
    }

    private void registerBaseCommonDao(BeanDefinitionRegistry registry) {
        /**
         * GenericBeanDefinition jdbcDaoProxyDefinition = new GenericBeanDefinition();
         * jdbcDaoProxyDefinition.setBeanClass(JDSupport.class);
         * registry.registerBeanDefinition("JDSupport", jdbcDaoProxyDefinition);
         */
    }
}
