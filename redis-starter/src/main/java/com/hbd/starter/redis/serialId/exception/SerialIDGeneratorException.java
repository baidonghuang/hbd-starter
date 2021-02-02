package com.hbd.starter.redis.serialId.exception;

/**
 * 流水号生成异常
 * @Author: hbd
 * @Date: 2019/6/03
 */
public class SerialIDGeneratorException extends RuntimeException{
    private static final long serialVersionUID = 3455708526465670030L;
    private String code;

    public SerialIDGeneratorException(String msg){
        super(msg);
    }

    public SerialIDGeneratorException(String code, String msg){
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
