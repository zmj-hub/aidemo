package com.enterprise.ai;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "企业级AI应用平台",
        version = "1.0.0",
        description = "基于Spring Boot 3和LangChain4j的企业级AI应用平台",
        contact = @Contact(name = "AI Platform Team")
    )
)
public class AiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiApplication.class, args);
        log.warn("启动完毕");
    }
}
