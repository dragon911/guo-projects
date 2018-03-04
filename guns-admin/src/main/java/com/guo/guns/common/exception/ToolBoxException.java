package com.guo.guns.common.exception;

/**
 * Created by guo on 3/4/2018.
 * 工具类初始化
 */
public class ToolBoxException extends RuntimeException {
    //serialVersionUID 是 Java 为每个序列化类产生的版本标识，可用来保证在反序列时，发送方发送的和接受方接收的是可兼容的对象。
    // 如果接收方接收的类的 serialVersionUID 与发送方发送的 serialVersionUID 不一致，进行反序列时会抛出 InvalidClassException。序列化的类可显式声明 serialVersionUID 的值，
    private static final long serialVersionUID = 8247610319171014183L;

    public ToolBoxException(Throwable e) {
        super(e.getMessage(),e);
    }

    public ToolBoxException(String message) {
        super(message);
    }

    public ToolBoxException(String message, Throwable throwable) {
        super(message,throwable);
    }
    public ToolBoxException(String messageTemplate, Object...params) {
        super(StrKit.format(messageTemplate,params));
    }
}
