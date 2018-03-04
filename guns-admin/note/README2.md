### common.exception 异常定义

1、首先来看所有业务异常的枚举类，看异常大概就能知道这个系统主要完成那些业务逻辑。

```java
/**
 * 所有业务异常的枚举
 */
public enum BizExceptionEnum {

    /**
     * 字典
     */
    DICT_EXISTED(400,"字典已经存在"),
    ERROR_CREATE_DICT(500,"创建字典失败"),
    ERROR_WRAPPER_FIELD(500,"包装字典属性失败"),

    /**
     * 文件上传
     */
    FILE_READING_ERROR(400,"FILE_READING_ERROR!"),
    FILE_NOT_FOUND(400,"FILE_NOT_FOUND!"),
    UPLOAD_ERROR(500,"上传图片出错"),

    /**
     * 权限和数据问题
     */
    DB_RESOURCE_NULL(400,"数据库中没有该资源"),
    NO_PERMITION(405, "权限异常"),
    REQUEST_INVALIDATE(400,"请求数据格式不正确"),
    INVALID_KAPTCHA(400,"验证码不正确"),
    CANT_DELETE_ADMIN(600,"不能删除超级管理员"),
    CANT_FREEZE_ADMIN(600,"不能冻结超级管理员"),
    CANT_CHANGE_ADMIN(600,"不能修改超级管理员角色"),

    /**
     * 账户问题
     */
    USER_ALREADY_REG(401,"该用户已经注册"),
    NO_THIS_USER(400,"没有此用户"),
    USER_NOT_EXISTED(400, "没有此用户"),
    ACCOUNT_FREEZED(401, "账号被冻结"),
    OLD_PWD_NOT_RIGHT(402, "原密码不正确"),
    TWO_PWD_NOT_MATCH(405, "两次输入密码不一致"),

    /**
     * 错误的请求
     */
    MENU_PCODE_COINCIDENCE(400,"菜单编号和副编号不能一致"),
    EXISTED_THE_MENU(400,"菜单编号重复，不能添加"),
    DICT_MUST_BE_NUMBER(400,"字典的值必须为数字"),
    REQUEST_NULL(400, "请求有错误"),
    SESSION_TIMEOUT(400, "会话超时"),
    SERVER_ERROR(500, "服务器异常");

    BizExceptionEnum(int code, String message) {
        this.friendlyCode = code;
        this.friendlyMsg = message;
    }

    private int friendlyCode;
    private String friendlyMsg;
    private String urlPath;
    //Setter,Getter,Constractor略
    BizExceptionEnum(int code, String message) {
        this.friendlyCode = code;
        this.friendlyMsg = message;
    }
}
```

2、对业务异常的封装，首先需要注意的是继承自RuntimeException，之前讲过了[点这里](https://juejin.im/post/5a98f8ca6fb9a028bc2d30d5)

```java
/**
 * 业务异常的封装
 */
public class BussinessException extends RuntimeException {

    //友好提示的code码
    private int friendlyCode;

    //友好提示
    private String friendlyMsg;

    //业务异常调整页面
    private String urlPath;

    public BussinessException(BizExceptionEnum bizExceptionEnum) {
        this.friendlyCode = bizExceptionEnum.getCode();
        this.friendlyMsg = bizExceptionEnum.getMessage();
        this.urlPath = bizExceptionEnum.getUrlPath();
    }
}
```
3、接下来是工具类初始化异常，需要注意serialVersionUID的作用:

- 1、serialVersionUID 是 Java 为每个序列化类产生的版本标识，可用来保证在反序列时，发送方发送的和接受方接收的是可兼容的对象。
- 2、如果接收方接收的类的 serialVersionUID 与发送方发送的 serialVersionUID 不一致，进行反序列时会抛出 InvalidClassException。
- 3、序列化的类可显式声明 serialVersionUID 的值，

这个中定义异常和PayMap中定义方式几乎一样。
```java
/**
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
--------------------------------------------------------------------------------
/**
 * 验证码错误异常
 *
 * @Author guo             //这个模板不错
 * @Date 2018-03-04 12:04.
 */
public class InvalidKaptchaException extends RuntimeException {
}
```

4、最后在看下自定义注解

元注解：

元注解的作用就是负责注解其他注解。Java5.0定义了4个标准的meta-annotation类型，它们被用提供对其他annotation类型的说明。

- 1、@Target
- 2、@Retention
- 3、@Documented
- 4、@Inherited
-
**@Target**

作用：用于描述注解的使用范围（即：被描述的注解可以用在什么地方）

取值(ElementType)有：

- 1.CONSTRUCTOR:用于描述构造器
- 2.FIELD:用于描述域
- 3.LOCAL_VARIABLE:用于描述局部变量
- 4.METHOD:用于描述方法
- 5.PACKAGE:用于描述包
- 6.PARAMETER:用于描述参数
- 7.TYPE:用于描述类、接口(包括注解类型) 或enum声明

**@Retention**

作用：表示需要在什么级别保存该注释信息，用于描述注解的生命周期（即：被描述的注解在什么范围内有效）

取值（RetentionPoicy）有：

- 1.SOURCE:在源文件中有效（即源文件保留）
- 2.CLASS:在class文件中有效（即class保留）
- 3.RUNTIME:在运行时有效（即运行时保留）

**@Documented**

作用：用于描述其它类型的annotation应该被作为被标注的程序成员的公共API，因此可以被例如javadoc此类的工具文档化。

**@Inherited**

@Inherited元注解是一个标记注解，@Inherited阐述了某个被标注的类型是被继承的。如果一个使用了@Inherited修饰的annotation类型被用于一个class，则这个annotation将被用于该class的子类。

接下来，我们在看自定义注解

```java
/**
 * 权限注解，用于检查权限 规定访问权限
 *
 * @Author guo
 * @Date 2018-03-04 12:15.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)   //运行时有效
@Target({ElementType.METHOD})         //方法范围
public @interface Permission {
    String[] value() default {};
}
-------------------------------------------------------
/**
 * 多数据源标识

 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface DataSource {
}


```
