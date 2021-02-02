package com.hbd.starter.access.supervisor.entity;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class CtlAccessLogCommonEntity {

    private String id;
    private Integer businessType;
    private String sourcePlatform;
    private String requestUniqueId;
    private String requestUrl;
    private String requestJson;
    private String responseJson;
    private String exceptionMessage;
    private String remoteIp;
    private String visitorIdentifier;
    private Date createdTime;
    private Boolean status;

}
