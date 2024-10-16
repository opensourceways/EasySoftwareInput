package com.easysoftwareinput.easysoftwareinput.field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.easysoftwareinput.application.fieldpkg.FieldPkgService;
@SpringBootTest
public class FieldPkgServiceTest {
    @Autowired
    private FieldPkgService field;
    @Test
    public void test() {
        field.run();
    }

}
