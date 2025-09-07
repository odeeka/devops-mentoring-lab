package com.example.hello;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HelloApiIT {
  @LocalServerPort int port;
  @Autowired TestRestTemplate rest;

  @Test void hello_overHttp() {
    var body = rest.getForObject("http://localhost:"+port+"/hello?name=Tibor", String.class);
    assertThat(body).isEqualTo("Hello, Tibor!");
  }
}
