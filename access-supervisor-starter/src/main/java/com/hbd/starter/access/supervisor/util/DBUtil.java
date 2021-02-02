package com.hbd.starter.access.supervisor.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.hbd.starter.access.supervisor.entity.CtlAccessLogCommonEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.*;
import java.util.List;

@Slf4j
@Component
public class DBUtil {

    @Resource(name = "accessLogDatasource")
    private DruidDataSource dataSource;

    public void insert(String tableSuffix, CtlAccessLogCommonEntity entity) throws SQLException {
        String sql = "insert into ctl_access_log_"+tableSuffix+"(id, business_type, source_platform," +
                "                  request_url, request_json, response_json, exception_message," +
                "                  status, created_time, remote_ip, visitor_identifier, request_unique_id)" +
                "          values(?, ?, ?," +
                "                 ?, ?, ?, ?," +
                "                 ?, ?, ?, ?, ?) ";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            // 关闭自动提交
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, entity.getId());
            pstmt.setInt(2, entity.getBusinessType());
            pstmt.setString(3, entity.getSourcePlatform());
            pstmt.setString(4, entity.getRequestUrl());
            pstmt.setString(5, entity.getRequestJson());
            pstmt.setString(6, entity.getResponseJson());
            pstmt.setString(7, entity.getExceptionMessage());
            pstmt.setBoolean(8, entity.getStatus());
            pstmt.setTimestamp(9, new Timestamp(entity.getCreatedTime().getTime()));
            pstmt.setString(10, entity.getRemoteIp());
            pstmt.setString(11, entity.getVisitorIdentifier());
            pstmt.setString(12, entity.getRequestUniqueId());
            pstmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            try {
                // 事务回滚
                conn.rollback();
            } catch (SQLException e1) {
                log.error("回滚异常",e1);
            }
            log.error("插入日志异常",e);
            throw e;
        } finally {
            close(conn, pstmt);
        }
    }

    public void update(String tableSuffix, CtlAccessLogCommonEntity entity) {
        String sql="update ctl_access_log_"+tableSuffix+" set status=?, exception_message=?, response_json=? where id=?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(sql);
            pstmt.setBoolean(1, entity.getStatus());
            pstmt.setString(2, entity.getExceptionMessage());
            pstmt.setString(3, entity.getResponseJson());
            pstmt.setString(4, entity.getId());
            pstmt.executeUpdate();//更新操作
            conn.commit();
        } catch (SQLException sqle) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                log.error("日志更新回滚失败",e);
            }
            log.error("更新日志异常", sqle);
        } finally {
            close(conn, pstmt);
        }
    }

    public void close(Connection connection, Statement statement) {
        try {
            statement.close();
            connection.close();
        } catch (SQLException e) {
            log.error("连接关闭异常",e);
        }

    }

    public List<CtlAccessLogCommonEntity> list(String tableSuffix, CtlAccessLogCommonEntity logEntity) {
       /* id, business_type, source_platform," +
        "                  request_url, request_json, response_json, exception_message," +
                "                  status, created_time, remote_ip, visitor_identifier, request_unique_id)*/
        StringBuffer sql= new StringBuffer("select * from ctl_access_log_"+tableSuffix+" where 1=1 ");
        int paramSize = 0;
        if(BlankUtil.isNotEmpty(logEntity.getVisitorIdentifier())) {
            paramSize++;
            sql.append("and visitor_identifier = ?");
        }
        if(logEntity.getBusinessType() != null) {
            paramSize++;
            sql.append("and business_type = ?");
        }
        if(BlankUtil.isNotEmpty(logEntity.getRequestUniqueId())) {
            paramSize++;
            sql.append("and request_unique_id = ?");
        }
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.executeUpdate();//更新操作
            conn.commit();
        } catch (SQLException sqle) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                log.error("日志更新回滚失败",e);
            }
            log.error("更新日志异常", sqle);
        } finally {
            close(conn, pstmt);
        }
        return null;
    }
}
