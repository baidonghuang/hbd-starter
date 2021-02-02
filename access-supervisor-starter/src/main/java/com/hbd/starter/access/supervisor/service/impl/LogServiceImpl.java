package com.hbd.starter.access.supervisor.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.hbd.starter.access.supervisor.annotation.AccessRecord;
import com.hbd.starter.access.supervisor.entity.CtlAccessLogCommonEntity;
import com.hbd.starter.access.supervisor.enums.PersistenceStrategy;
import com.hbd.starter.access.supervisor.service.LogService;
import com.hbd.starter.access.supervisor.util.BaseThreadPoolUtil;
import com.hbd.starter.access.supervisor.util.DBUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Service
@ConditionalOnProperty(name = "ctl.access.supervisor.enable", havingValue = "true", matchIfMissing = false)
public class LogServiceImpl implements LogService {

    @Resource
    private DBUtil dbUtil;

    @Override
    public void insertLog(CtlAccessLogCommonEntity logEntity, AccessRecord annotation) throws SQLException {
        log.info(" 记录操作日志_accessSupervisorLog_:{}", JSONObject.toJSONString(logEntity, SerializerFeature.WriteDateUseDateFormat));
        if(PersistenceStrategy.ASYNC_DB == annotation.persistence()) {
            BaseThreadPoolUtil.getInstance().execute(() -> {
                try {
                    dbUtil.insert(annotation.tableSuffix(), logEntity);
                } catch (SQLException throwables) {
                }
            });
        } else if(PersistenceStrategy.SYNC_DB == annotation.persistence()){
            dbUtil.insert(annotation.tableSuffix(), logEntity);
        }
    }

    @Override
    public void updateLog(CtlAccessLogCommonEntity logEntity, AccessRecord annotation) {
        log.info(" 更新操作日志_accessSupervisorLog_:{}", JSONObject.toJSONString(logEntity, SerializerFeature.WriteDateUseDateFormat));
        if(PersistenceStrategy.ASYNC_DB == annotation.persistence()) {
            BaseThreadPoolUtil.getInstance().execute(() -> {
                dbUtil.update(annotation.tableSuffix(), logEntity);
            });
        } else if(PersistenceStrategy.SYNC_DB == annotation.persistence()){
            dbUtil.update(annotation.tableSuffix(), logEntity);
        }
    }

    @Override
    public List<CtlAccessLogCommonEntity> list(String tableSuffix, CtlAccessLogCommonEntity logEntity) {
        return dbUtil.list(tableSuffix, logEntity);
    }
}
