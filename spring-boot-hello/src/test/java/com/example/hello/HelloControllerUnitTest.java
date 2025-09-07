package com.example.hello;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HelloControllerUnitTest {
  @Test void hello_withName() {
    var c = new HelloController();
    assertEquals("Hello, Tibor!", c.hello("Tibor"));
  }
}
