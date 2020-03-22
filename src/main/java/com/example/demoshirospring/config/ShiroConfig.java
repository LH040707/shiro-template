package com.example.demoshirospring.config;

import jdk.nashorn.internal.parser.Token;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.mgt.SubjectFactory;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.DefaultWebSubjectFactory;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class ShiroConfig {

    // 1.配置 SecurityManager（关键）
    @Bean("securityManager")
    @Autowired
    public SecurityManager securityManager(
            @Qualifier("cacheManager") CacheManager cacheManager,
            @Qualifier("realm") Realm realm
    ) {
        DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
        manager.setCacheManager(cacheManager);
        manager.setRealm(realm);
        return manager;
    }

    // 2.配置 CacheManger.
    @Bean("cacheManager")
    public CacheManager cacheManager() {
        EhCacheManager manager = new EhCacheManager();
        return manager;
    }

    // 3.配置Realm（关键）
    @Bean("realm")
    public Realm realm() {
        // 直接实现了 Realm 接口的 bean
        // 实际开发中，要么有现成的，要么有必要单独一个文档实现接口
        return new AuthorizingRealm() {
            @Override
            protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
                // 暂时不处理
                return null;
            }

            @Override
            protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
                // 密码为 pw 即可通过认证
                return new SimpleAuthenticationInfo(new Object(), "pw", "realName");
            }

        };
    }

    // 4.配置 Shiro 生命周期处理器.
    // 用于在实现了 Initializable 接口的 Shiro bean 初始化时调用 Initializable 接口回调(例如:CasRealm)
    // 在实现了 Destroyable 接口的 Shiro bean 销毁时调用 Destroyable 接口回调(例如:DefaultSecurityManager)
    // 可以自动的来调用配置在 Spring IOC 容器中 shiro bean 的生命周期方法.
    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    // 5.启用 IOC 容器中使用 shiro 的注解.
    // 但必须在配置了 LifecycleBeanPostProcessor 之后才可以使用.
    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        return new DefaultAdvisorAutoProxyCreator();
    }

    // 6.配置 ShiroFilter（关键）
    // bean 名必须和下面 DelegatingFilterProxy 构造时传入的 beanName 一致
    @Bean("shiroFilter")
    @Autowired
    public ShiroFilterFactoryBean shiroFilterFactoryBean(
            @Qualifier("securityManager") SecurityManager securityManager
    ) {
        ShiroFilterFactoryBean factory = new ShiroFilterFactoryBean();
        factory.setSecurityManager(securityManager);

        // 检测到用户未登录后，转跳的页面（通常为登录页URL）
        factory.setLoginUrl("/index");
        // 验证成功的后的默认转跳路径(若有指定路径，则不走此默认路径)
        factory.setSuccessUrl("/index");
        // 没有访问权限的转跳路径
        factory.setUnauthorizedUrl("/unauthorizedUrl");

        // 配置访问这些页面的权限
        // (本质上，key是URL，value是Filter链)
        /*
            anon    可以被匿名访问
            authc   必须认证后，才可以访问的页面
            user    必须荣有 记住我 功能才能用
            perms   拥有对某个资源的权限才能访问
            roles    荣有某个角色权限才能访问
            ...
            以上定义的枚举类为：org.apache.shiro.web.filter.mgt.DefaultFilter
         */
        Map<String, String> map = factory.getFilterChainDefinitionMap();
        // 必须加 "/" 斜杠
        map.put("/favicon.ico", "anon");
        map.put("/login", "anon");
        map.put("/index", "anon");
        // 表示该 uri 需要认证用户拥有 admin 角色才能访问(测试 setUnauthorizedUrl 拦截)
        map.put("/user", "roles[user]");
        map.put("/logined", "authc");
        map.put("/**", "authc");
        return factory;
    }

    // 7.注册自定义Filter (扩展)
 /*   @Bean
    @Autowired
    public FilterRegistrationBean<DelegatingFilterProxy> logoutFilterRegistration(
            @Qualifier("securityManager") SecurityManager securityManager
    ) {
        LogoutFilter filter = new LogoutFilter();
        // 登出后转跳的 URL
        filter.setRedirectUrl("/index");

        // 把 servlet 容器中的 filter 同 spring 容器中的 bean 关联起来
        FilterRegistrationBean<DelegatingFilterProxy> registration = new FilterRegistrationBean<>();
        DelegatingFilterProxy proxy= new DelegatingFilterProxy(filter);
        registration.setFilter(proxy);
        // 注册的拦截 拦截的URL
        registration.addUrlPatterns("/logout");
        registration.setOrder(1);
        return registration;
    }*/

}
