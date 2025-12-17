package com.educationalplatform.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class OpenApiConfiguration {

  private static final String URL_TEMPLATE = "https://%s";

  @Value("${global.domain}")
  private String domain;

  @Bean
  public OpenAPI customOpenAPI() {
    String url = String.format(URL_TEMPLATE, domain);

    return new OpenAPI()
        .servers(List.of(
            new Server().url(url)
        ));
  }
}