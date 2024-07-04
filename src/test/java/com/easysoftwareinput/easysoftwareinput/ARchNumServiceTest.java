package com.easysoftwareinput.easysoftwareinput;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.easysoftwareinput.application.archnum.ArchNumService;

@SpringBootTest
public class ARchNumServiceTest {
    @Autowired
    private ArchNumService service;

    @Test
    public void test() {
        service.run();
        service.validData();
    }
}
