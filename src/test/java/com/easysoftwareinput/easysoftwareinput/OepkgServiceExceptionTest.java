package com.easysoftwareinput.easysoftwareinput;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import org.apache.ibatis.exceptions.PersistenceException;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import com.easysoftwareinput.application.oepkg.OepkgService;
import com.easysoftwareinput.infrastructure.mapper.OepkgDOMapper;
import com.easysoftwareinput.infrastructure.oepkg.OepkgGatewayImpl;

@SpringBootTest
public class OepkgServiceExceptionTest {
    @Autowired
    OepkgDOMapper mapper;

    @Autowired
    OepkgService service;

    @MockBean
    OepkgGatewayImpl gateway;

    @Test
    public void test_drop_connection() {
        Exception e = new MyBatisSystemException(
                new PersistenceException(
                new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection")));
        when(gateway.getChangedRow(System.currentTimeMillis())).thenThrow(e);

        mapper.delete(null);
        service.run();
        assertFalse(service.validData());
    }
}
