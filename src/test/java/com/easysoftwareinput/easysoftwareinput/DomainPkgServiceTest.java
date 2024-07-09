package com.easysoftwareinput.easysoftwareinput;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.easysoftwareinput.application.domainpackage.DomainPkgService;

@SpringBootTest
public class DomainPkgServiceTest {
    @Autowired
    private DomainPkgService service;

    @Test
    public void test() {
        service.run();
        assertTrue(service.validData());
    }
}
