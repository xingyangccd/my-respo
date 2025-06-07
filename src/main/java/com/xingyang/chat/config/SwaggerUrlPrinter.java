package com.xingyang.chat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Swagger URL Printer - Logs API documentation URLs on application startup
 *
 * @author XingYang
 */
@Slf4j
@Component
public class SwaggerUrlPrinter implements ApplicationRunner {

    @Value("${server.port:8080}")
    private int serverPort;
    
    @Value("${server.servlet.context-path:/api}")
    private String contextPath;
    
    @Override
    public void run(ApplicationArguments args) {
        log.info("API documentation is available at:");
        log.info("Knife4j URL: http://localhost:{}{}/doc.html", serverPort, contextPath);
        log.info("Swagger UI URL: http://localhost:{}{}/swagger-ui/index.html", serverPort, contextPath);
    }
} 