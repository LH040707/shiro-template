package com.example.demoshirospring.controller;

import javafx.css.SimpleStyleableStringProperty;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShiroController {

    @RequestMapping("/login")
    public String login() {
        AuthenticationToken token = new UsernamePasswordToken(
                "a"
                , "pw");
        SecurityUtils.getSubject().login(token);
        return "login";
    }

    @RequestMapping("/index")
    public String index() {
        Subject subject = SecurityUtils.getSubject();
        return "首页，是否登录：" + subject.isAuthenticated();
    }

    @RequestMapping("/unauthorizedUrl")
    public String unauthorizedUrl() {
        return "unauthorizedUrl";
    }

    @RequestMapping("/logout")
    public String logout() {
        SecurityUtils.getSubject().logout();
        return "logout ok!";
    }

    //需要 user 用户权限
    @RequestMapping("/user")
    public String list() {
        return "user role ";
    }

    //需要 登录才可访问
    @RequestMapping("/logined")
    public String getA() {
        return "has login";
    }

}
