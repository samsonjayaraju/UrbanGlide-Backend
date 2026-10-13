package com.urbanglide.driverservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@org.springframework.cloud.openfeign.EnableFeignClients
@SpringBootApplication
public class DriverServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(DriverServiceApplication.class, args);
  }
}
