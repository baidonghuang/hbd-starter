package com.hbd.starter.access.supervisor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbd.starter.access.supervisor.entity.CtlAccessLogCommonEntity;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 片区表 Mapper 接口
 * </p>
 *
 * @author cgs
 * @since 2019-04-28
 */
public interface CtlAccessLogCommonMapper extends BaseMapper<CtlAccessLogCommonEntity> {

    void insertLog(@Param("tableSuffix") String tableSuffix, @Param("po") CtlAccessLogCommonEntity accessLogCommonEntity);
}
