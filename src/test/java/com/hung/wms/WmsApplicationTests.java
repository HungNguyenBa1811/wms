package com.hung.wms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// Runs on the H2 "test" profile (src/test/resources/application-test.yaml), no MySQL needed
@SpringBootTest
@ActiveProfiles("test")
class WmsApplicationTests {

    @Test
    void contextLoads() {
    }

}
