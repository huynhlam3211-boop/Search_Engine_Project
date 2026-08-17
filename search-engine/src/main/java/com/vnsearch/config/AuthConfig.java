package com.vnsearch.config;

import com.vnsearch.auth.JsonUserStore;
import com.vnsearch.auth.Role;
import com.vnsearch.auth.SessionStore;
import com.vnsearch.auth.UserService;
import com.vnsearch.auth.UserStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Clock;

@Configuration
public class AuthConfig { 
    
}