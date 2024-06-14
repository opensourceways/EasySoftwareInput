package com.easysoftwareinput.easysoftwareinput;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.sql.SQLNonTransientConnectionException;

import org.apache.ibatis.exceptions.PersistenceException;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import com.easysoftwareinput.application.rpmpackage.RPMPackageService;
import com.easysoftwareinput.infrastructure.mapper.RPMPackageDOMapper;
import com.easysoftwareinput.infrastructure.rpmpkg.RpmGatewayImpl;

@SpringBootTest
public class RPMPackageServiceExceptionTest {
    @Autowired
    RPMPackageDOMapper mapper;

    @Autowired
    RPMPackageService service;

    @MockBean
    RpmGatewayImpl gateway;

    @Test
    public void test_drop_connection() {
        Exception e = new MyBatisSystemException(
                new PersistenceException(
                new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection")));
        when(gateway.getChangedRow(service.getStartTime())).thenThrow(e);

        mapper.delete(null);
        service.run();
        assertFalse(service.validData());
    }
}
