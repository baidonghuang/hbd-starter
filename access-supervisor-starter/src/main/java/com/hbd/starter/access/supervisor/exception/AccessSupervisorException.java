package com.hbd.starter.access.supervisor.exception;

/**
 * 日志记录异常
 * @Author: hbd
 * @Date: 2021/01/13
 */
public class AccessSupervisorException extends RuntimeException {
    private static final long serialVersionUID = 3455708526465670030L;
    private String code;

    public AccessSupervisorException(String msg){
        super(msg);
    }

    public AccessSupervisorException(String code, String msg){
        super(msg);
        this.code=code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
