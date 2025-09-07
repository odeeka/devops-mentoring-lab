package com.example.hello;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = HelloController.class)
class HelloApiWebMvcTest {
  @Autowired MockMvc mvc;

  @Test void hello_endpoint() throws Exception {
    mvc.perform(get("/hello").param("name","Tibor"))
      .andExpect(status().isOk())
      .andExpect(content().string("Hello, Tibor!"));
  }
}
