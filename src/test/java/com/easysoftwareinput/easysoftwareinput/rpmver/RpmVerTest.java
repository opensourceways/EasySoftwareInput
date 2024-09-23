package com.easysoftwareinput.easysoftwareinput.rpmver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.easysoftwareinput.application.appver.RpmVerMonitorAliasService;

@SpringBootTest
public class RpmVerTest {
    @Autowired
    private RpmVerMonitorAliasService service;

    @Test
    public void test() {
        assertEquals(service.getMonitorName("iproute"), "iproute2");
        assertEquals(service.getMonitorName("gtk2"), "gtk%2B");
    }
}
