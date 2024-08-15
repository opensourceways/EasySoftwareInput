package com.easysoftwareinput.repopkgname;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.easysoftwareinput.application.repopkgnamemapper.RepoPkgNameMapperService;

@SpringBootTest
public class RepoPkgNameTest {
    @Autowired
    private RepoPkgNameMapperService service;

    /**
     * 正常情况
     */
    @Test
    public void test() {
        assertTrue(service.run());
    }
}
