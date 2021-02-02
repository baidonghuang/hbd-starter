package com.hbd.starter.access.supervisor.service;

import com.hbd.starter.access.supervisor.annotation.AccessRecord;
import com.hbd.starter.access.supervisor.entity.CtlAccessLogCommonEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
@ConditionalOnProperty(name = "ctl.access.supervisor.enable", havingValue = "true", matchIfMissing = false)
public interface LogService {

    void insertLog(CtlAccessLogCommonEntity logEntity, AccessRecord annotation) throws SQLException;

    void updateLog(CtlAccessLogCommonEntity logEntity, AccessRecord annotation);

    List<CtlAccessLogCommonEntity> list(String tableSuffix, CtlAccessLogCommonEntity logEntity);

}
