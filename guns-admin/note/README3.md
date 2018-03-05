
### 3、Spring Boot 自动配置

## 说明
**如果您有幸能看到，请认阅读以下内容；**
- 1、本项目临摹自[abel533](https://github.com/abel533/guns)的Guns，他的项目 fork 自 [stylefeng](https://gitee.com/naan1993) 的 [Guns](https://git.oschina.net/naan1993/guns)！开源的世界真好，可以学到很多知识。

- 2、版权归原作者所有，自己只是学习使用。跟着大佬的思路，希望自己也能变成大佬。gogogo》。。

- 3、目前只是一个后台模块，希望自己技能增强到一定时，可以把[stylefeng](https://gitee.com/naan1993) 的 [Guns]融合进来。
- 4、note里面是自己的学习过程，菜鸟写的，不是大佬写的。内容都是大佬的。

本来想一步一步的来，但是工具类快把我看晕了。所以我们还是先来看配置类吧，这才是今天的主角。先从数据库，日志，缓存开始。

想说明的是SpringBoot有四个重要的特性：

- 起步依赖 ：起步依赖其实就是特殊的Maven依赖和Gradle依赖，利用了传递依赖解析，把常用库聚合在一起，组成一个特定功能而制定的依赖。
- 自动配置 ：针对很多Spring应用常见的应用功能，SpringBoot能够提供相关配置，(底层帮我们做了很多事)
- 命令行界面：无需传统项目构建，
- Actuator：让你能够深入运行的SpringBott应用程序，一探究竟。

目前重要的是理解前两个，只要看见这个`spring-boot-starter-Xxx`它就属于起步依赖。会自动导入想依赖的库。

```xml
<parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>1.5.3.RELEASE</version>
        <relativePath/>
</parent>
-------------------------------------------------------------------------------
<dependencies>
    <!--spring boot依赖-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-aop</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```
**《SpringBoot实战》小节** 有机会一定要看《Spring实战》是同一个作者。结合代码效果更佳。[实战练习笔记](https://github.com/guoxiaoxu/SpringInActionPractice)

SpringBoot为Spring应用程序的开发提供了一种激动人心的新方式，框架本身带来的阻力很小，自动配置消除了传统Spring应用程序里很多的样板配置，Spring的起步依赖让你能通过库所提供的功能而非名称与版本号来指定构建依赖。

### 数据库配置
2、接下来，回到我们项目中的配置吧，先从阿里的druid。WebConfig一般是配置的起点。带有`@Configuration`注解的就意味着这是一个配置类。还有就是`@Bean`注解。bean的定义之前在XMl中形式为`<bean id ="xx" class="xx.xx.xx" />`

在spring boot中添加自己的Servlet、Filter、Listener有两种方法

- 通过代码注册：`ServletRegistrationBean`、`FilterRegistrationBean`、`ServletListenerRegistrationBean`获得控制/
- 注解注册：在SpringBootApplication上使用@ServletCompanentScan注解后，Servlet、Filter、Listener可以通过@WebServlet、@WebFilter、@WebListener注解自动注册，无需其他代码。

```java
/**
 * web 配置类  还有很多
 */
@Configuration
public class WebConfig {

    /**
     * druidServlet注册
     */
    @Bean
    public ServletRegistrationBean druidServletRegistration() {
        ServletRegistrationBean registration = new ServletRegistrationBean(new StatViewServlet());
        registration.addUrlMappings("/druid/*");
        return registration;
    }

    /**
     * druid监控 配置URI拦截策略
     */
    @Bean
    public FilterRegistrationBean druidStatFilter(){
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new WebStatFilter());
        //添加过滤规则.
        filterRegistrationBean.addUrlPatterns("/*");
        //添加不需要忽略的格式信息.
        filterRegistrationBean.addInitParameter(
                "exclusions","/static/*,*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid,/druid/*");
        //用于session监控页面的用户名显示 需要登录后主动将username注入到session里
        filterRegistrationBean.addInitParameter("principalSessionName","username");
        return filterRegistrationBean;
    }

    /**
     * druid数据库连接池监控
     */
    @Bean
    public DruidStatInterceptor druidStatInterceptor() {
        return new DruidStatInterceptor();
    }

    /**
     * druid数据库连接池监控
     */
    @Bean
    public BeanTypeAutoProxyCreator beanTypeAutoProxyCreator() {
        BeanTypeAutoProxyCreator beanTypeAutoProxyCreator = new BeanTypeAutoProxyCreator();
        beanTypeAutoProxyCreator.setTargetBeanType(DruidDataSource.class);
        beanTypeAutoProxyCreator.setInterceptorNames("druidStatInterceptor");
        return beanTypeAutoProxyCreator;
    }

    /**
     * druid 为druidStatPointcut添加拦截
     * @return
     */
    @Bean
    public Advisor druidStatAdvisor() {
        return new DefaultPointcutAdvisor(druidStatPointcut(), druidStatInterceptor());
    }

}
```

3、接下来我们在看看数据源的配置，先摘抄点我之前的笔记。配置H2数据库和JDBC的。

H2是一个开源的嵌入式数据库引擎，采用java语言编写，不受平台的限制，同时H2提供了一个十分方便的web控制台用于操作和管理数据库内容。H2还提供兼容模式，可以兼容一些主流的数据库，因此采用H2作为开发期的数据库非常方便。(数据存储在内存中)。

还需要注意的是`DataSource`数据源主要有两种方式实现：

- 1、直接数据库连接，因为每次都要进行三次握手(远程)，所有性能较差。
- 2、就是采用池化技术，比如上面说的阿里的druid(号称性能最强大，安全措施，还可以监控)，之前最常用的是C3PO，DBCP，需要的时候直接从池子中拿，用完直接还回去。DataSource实现原理是对连接进行缓存，从而提高效率，资源的重复利用。


```java
@Configuration
public class DataConfig {

  @Bean
  public DataSource dataSource() {
    return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript("schema.sql")
            .addScript("my-test-data.sql")
            .build();
  }
-----------------------------------------------------------------------------
  @Bean
  public JdbcOperations jdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

}
```
4、需要补充一点的是：老外很多都在用底层的JDBC技术，因为原生，效率高。`jdbcTemplate`是Spring对JDBC进一步封装。命名参数的使用。这种思想理解了吗？

其实还有一种更绝绝的那就是Spring Date。只要继承了`Repository`接口，你就拥有了18个方法，不满足你的话，还可以自己定义，还有一个就是`JpaRepository`建议了解下。

```java
private static final String SELECT_SPITTLE = "select sp.id, s.id as spitterId, s.username, s.password, s.fullname, s.email, s.updateByEmail, sp.message, sp.postedTime from Spittle sp, Spitter s where sp.spitter = s.id";
private static final String SELECT_SPITTLE_BY_ID = SELECT_SPITTLE + " and sp.id=?";
private static final String SELECT_SPITTLES_BY_SPITTER_ID = SELECT_SPITTLE + " and s.id=? order by sp.postedTime desc";
private static final String SELECT_RECENT_SPITTLES = SELECT_SPITTLE + " order by sp.postedTime desc limit ?";

public List<Spittle> findBySpitterId(long spitterId) {
  return jdbcTemplate.query(SELECT_SPITTLES_BY_SPITTER_ID, new SpittleRowMapper(), spitterId);
}
public List<Spittle> findBySpitterId(long spitterId) {
  return jdbcTemplate.query(SELECT_SPITTLES_BY_SPITTER_ID, new SpittleRowMapper(), spitterId);
}
```

5、接下来我们就是配置数据源了，

本来想录个Gif，但我软件出BUG了，有什么好推荐的么？为了不占地方，只放一张。关于日志的，自行脑补。好想给大家分享我的书签，太多有用的了。

![](https://i.imgur.com/4wZmhId.jpg)

- @Component spring初始化的时候，spring会把所有添加@Component注解的类作为使用自动扫描注入配置路径下的备选对象，同时在初始化spring的@Autowired
- 　@Controller注解是一个特殊的Component，它允许了实现类可以通过扫描类配置路径的方式完成自动注入，通常@Controller是结合@RequestMapping注解一起使用的。
- @ConfigurationProperties 注解用于外部化(externalized)配置，提供 prefix 和 locations 两个属性指定属性文件的来源和属性的前缀

```java
/**
 * <p>数据库数据源配置</p>
 * <p>说明:这个类中包含了许多默认配置,若这些配置符合您的情况,您可以不用管,若不符合,建议不要修改本类,建议直接在"application.yml"中配置即可</p>
 */
@Component
@ConfigurationProperties(prefix = "spring.datasource")
public class DruidProperties {

    private String url = "jdbc:mysql://127.0.0.1:3306/guns?autoReconnect=true&useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull";

    private String username = "root";

    private String password = "632364";

    private String driverClassName = "com.mysql.jdbc.Driver";
    //为了节约地方就不都贴出来了。

    public void config(DruidDataSource dataSource) {

        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        dataSource.setDriverClassName(driverClassName);
        dataSource.setInitialSize(initialSize);     //定义初始连接数
        dataSource.setMinIdle(minIdle);             //最小空闲
        dataSource.setMaxActive(maxActive);         //定义最大连接数
        dataSource.setMaxWait(maxWait);             //最长等待时间

        // 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
        dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);

        // 配置一个连接在池中最小生存的时间，单位是毫秒
        dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        dataSource.setValidationQuery(validationQuery);
        dataSource.setTestWhileIdle(testWhileIdle);
        dataSource.setTestOnBorrow(testOnBorrow);
        dataSource.setTestOnReturn(testOnReturn);

        // 打开PSCache，并且指定每个连接上PSCache的大小
        dataSource.setPoolPreparedStatements(poolPreparedStatements);
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);

        try {
            dataSource.setFilters(filters);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
```
### 复习AOP

6、还有就是多数据源，采用切面织入。直接拿自己之前的笔记吧，

在软件开发中，散布于应用中多处的功能被称为横切关注点(crosscutting concern)。通常来讲横切关注点从概念上是与应用的业务逻辑分离的。但往往是耦合在一起的，把这些横切关注点与业务逻辑相分离正是面向切面编程(AOP)所要解决的问题。

依赖注入(DI)管理我们的应用对象，DI有助于应用对象之间解耦。而AOP可以实现横切关注点与它们所影响的对象之间的耦合。

横切关注点可以被模块化为特殊的类，这些类被称为切面(aspect). 这样做带来两个好处：每个关注点都集中到一个地方，而不是分散到多处代码中：其次，服务模块更简洁，因为它只包含了主要关注点(核心功能)的代码。而次要关注的代码被移到切面中了。

描述切面的常用术语有：通知(advice)、切点(pointcut)、(连接点)。

**通知(advice)**

通知定义了切面是什么以及何时使用。除了描述切面要完成的工作外，通知还解决了何时执行这个工作问题。它应该在某个方法被调用之前？之后？之前和之后都调用？还是只在方法抛出异常时调用？

Spring切面可以应用5中类型的通知：

- 前置通知(Before):在目标方法被调用之前调用通知功能。
- 后置通知(After):在目标方法完成之后调用通知
- 返回通知(After-returning):在目标方法成功执行之后调用通知
- 异常通知(After-throwing):在目标方法抛出异常后调用通知
- 环绕通知(Around):在被通知方法调用之前和调用之后执行自定义的行为

**连接点**

我们的应用可能有数以千计的时机应用通知，这些时机被称为连接点。连接点是在应用执行过程中能够插入的一个点。这个点可以是调用方法时，抛出异常时，甚至修改一个字段时。切面可以利用这些点插入到应用的正常流程之中，并添加新的行为。

**切点**

如果说通知定义了切面的的“什么”和“何时”，那么切点定义了“何处”。切点的定义会匹配通知所要织入的一个或多个连接点。

**切面**

切面是通知和切点的结合。通知和切点通过定义了切面的全部 内容——他是什么，在什么时候和在哪里完成其功能。

**引入** 引入允许我们向现有的类添加新的方法或者属性。

**织入**

织入是把切面应用到目标对象并创建新的代理对象的过程。切面在指定的连接点被织入到目标对象。在目标对象的生命周期里有多个点可以进行织入：

- 编译器：切面在目标类编译时被织入。Aspect的织入编译器就是以这种方式织入切面的。
- 类加载器：切面在目标类加载到JVM时被织入。需要特殊的类加载(Classloader)，它可以在目标类被引入之前增强该目标类的字节码(CGlib)
- 运行期：切面在应用运行时的某个时刻被织入。AOP会为目标对象创建一个代理对象
Spring提供了4种类型的AOP支持：

- 基于代理的经典Spring AOP
- 纯POJO切面
- @AspectJ注解驱动的切面
- 注入式AspectJ切面


7、带着上面的概念，我们在来看下多数据源的配置，先看一下测试效果：

首先所数据源作为一个切面，用@Aspect注解，然后定义了切点，只要使用@DataSource注解的方法它就是一个切点，简单说就是切面切在那个方法上。然后用@Around("cut()")定义了环绕通知，就是调用前和调用之后执行这个数据源。还有就是这里使用了日志记录功能，这个主题待会说。

```java
/**
 * 多数据源的枚举
 */
public interface DSEnum {

	String DATA_SOURCE_GUNS = "dataSourceGuns";		//guns数据源

	String DATA_SOURCE_BIZ = "dataSourceBiz";			//其他业务的数据源
}

--------------------------------------------------------------------------------
@Override
@DataSource(name = DSEnum.DATA_SOURCE_BIZ)
public void testBiz() {
    Test test = testMapper.selectByPrimaryKey(1);
    test.setId(22);
    testMapper.insert(test);
}

@Override
@DataSource(name = DSEnum.DATA_SOURCE_GUNS)
public void testGuns() {
    Test test = testMapper.selectByPrimaryKey(1);
    test.setId(33);
    testMapper.insert(test);
}
```
```java
/**
 *
 * 多数据源切换的aop
 */
@Aspect
@Component
@ConditionalOnProperty(prefix = "guns", name = "muti-datasource-open", havingValue = "true")
public class MultiSourceExAop implements Ordered {

	private Logger log = LoggerFactory.getLogger(this.getClass());


	@Pointcut(value = "@annotation(com.guo.guns.common.annotion.DataSource)")
	private void cut() {

	}

	@Around("cut()")
	public Object around(ProceedingJoinPoint point) throws Throwable {

		Signature signature = point.getSignature();
        MethodSignature methodSignature = null;
        if (!(signature instanceof MethodSignature)) {
            throw new IllegalArgumentException("该注解只能用于方法");
        }
        methodSignature = (MethodSignature) signature;

        Object target = point.getTarget();
        Method currentMethod = target.getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());

        DataSource datasource = currentMethod.getAnnotation(DataSource.class);
        if(datasource != null){
			DataSourceContextHolder.setDataSourceType(datasource.name());
			log.debug("设置数据源为：" + datasource.name());
        }else{
        	DataSourceContextHolder.setDataSourceType(DSEnum.DATA_SOURCE_GUNS);
			log.debug("设置数据源为：dataSourceCurrent");
        }

        try {
        	return point.proceed();
		} finally {
			log.debug("清空数据源信息！");
			DataSourceContextHolder.clearDataSourceType();
		}
	}
}
```
这个项目使用了Mybatis作为持久层框架，所以看看他是怎么配置的。要使用当然要注入了，这里使用了@Autowired注解。


在Spring中，对象无需自己查找或创建与其所关联的其他对象。相反，容器负责把需要相互协作的对象引用赋予各个对象。 一个订单管理组件需要信用卡认证组件，但它不需要自己创建信用卡认证组件，容器会主动赋予它一个人在组件。Spirng自动满足bean之间的依赖

@MapperScan：自动扫描mappers，将其关联到SqlSessionTemplate，并将mappers注册到spring容器中，以便注入到我们的beans中。


```java
/**
 * MybatisPlus配置
 */
@Configuration
@EnableTransactionManagement(order = 2)//由于引入多数据源，所以让spring事务的aop要在多数据源切换aop的后面
@MapperScan(basePackages = {"com.guo.guns.modular.*.dao", "com.guo.guns.common.persistence.dao"})
public class MybatisPlusConfig {

    @Autowired
    DruidProperties druidProperties;

    @Autowired
    MutiDataSourceProperties mutiDataSourceProperties;
    /**
     * 另一个数据源
     */
    private DruidDataSource bizDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        druidProperties.config(dataSource);
        mutiDataSourceProperties.config(dataSource);
        return dataSource;
    }
    //省略单数据源和guns数据源
    /**
     * 多数据源连接池配置
     */
    @Bean
    @ConditionalOnProperty(prefix = "guns", name = "muti-datasource-open", havingValue = "true")
    public DynamicDataSource mutiDataSource() {

        DruidDataSource dataSourceGuns = dataSourceGuns();
        DruidDataSource bizDataSource = bizDataSource();

        try {
            dataSourceGuns.init();    //重点
            bizDataSource.init();
        }catch (SQLException sql){
            sql.printStackTrace();
        }

        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        HashMap<Object, Object> hashMap = new HashMap();     //这里使用了HashMap
        hashMap.put(DSEnum.DATA_SOURCE_GUNS, dataSourceGuns);
        hashMap.put(DSEnum.DATA_SOURCE_BIZ, bizDataSource);
        dynamicDataSource.setTargetDataSources(hashMap);
        dynamicDataSource.setDefaultTargetDataSource(dataSourceGuns);
        return dynamicDataSource;
    }
-----------------------------待会说--------------------------------------------
    /**
     * 数据范围mybatis插件
     */
    @Bean
    public DataScopeInterceptor dataScopeInterceptor() {
        return new DataScopeInterceptor();
    }
}
```

看代码可以让问题变得更简单，

拦截器的一个作用就是我们可以拦截某些方法的调用，我们可以选择在这些被拦截的方法执行前后加上某些逻辑，也可以在执行这些被拦截的方法时执行自己的逻辑而不再执行被拦截的方法。
- @Intercepts用于表明当前的对象是一个Interceptor，
- @Signature则表明要拦截的接口、方法以及对应的参数类型。

原谅我没看懂。
```java

/**
 * 数据范围
 */
public class DataScope {
    /**
     * 限制范围的字段名称
     */
    private String scopeName = "deptid";
    /**
     * 限制范围的
     */
    private List<Integer> deptIds;
    //......
}
--------------------------------------------------------------------------------
/**
 * 数据范围的拦截器
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class DataScopeInterceptor implements Interceptor {

    /**
     * 获得真正的处理对象,可能多层代理.
     */
    public static Object realTarget(Object target) {
        if (Proxy.isProxyClass(target.getClass())) {
            MetaObject metaObject = SystemMetaObject.forObject(target);
            return realTarget(metaObject.getValue("h.target"));
        }
        return target;
    }
  //省略一大堆，回来在缕缕。
}
```
### 日志记录配置

数据部分就算配置完成了，接下来就是重要的日志部分。这个很重要，可具体记录哪个用户，执行了哪些业务，修改了哪些数据，并且**日志记录为异步执行，也是基于JavaConfig.**

老样子，先看工厂
```java
/**
 * 日志对象创建工厂
 */
public class LogFactory {

    /**
     * 创建操作日志
     */
    public static OperationLog createOperationLog(LogType logType, Integer userId, String bussinessName, String clazzName, String methodName, String msg, LogSucceed succeed) {
        OperationLog operationLog = new OperationLog();
        operationLog.setLogtype(logType.getMessage());
        operationLog.setLogname(bussinessName);
        operationLog.setUserid(userId);
        operationLog.setClassname(clazzName);
        operationLog.setMethod(methodName);
        operationLog.setCreatetime(new Date());
        operationLog.setSucceed(succeed.getMessage());
        operationLog.setMessage(msg);
        return operationLog;
    }
    //登录日志省略
}
---------------------------------------------------------------------------------
Timer是一种定时器工具，用来在一个后台线程计划执行指定任务。它可以计划执行一个任务一次或反复多次。
TimerTask一个抽象类，它的子类代表一个可以被Timer计划的任务。

/**
 * 日志操作任务创建工厂
 *
 * @author fengshuonan
 * @date 2016年12月6日 下午9:18:27
 */
public class LogTaskFactory {

    private static Logger logger             = LoggerFactory.getLogger(LogManager.class);
    private static LoginLogMapper loginLogMapper     = SpringContextHolder.getBean(LoginLogMapper.class);
    private static OperationLogMapper operationLogMapper = SpringContextHolder.getBean(OperationLogMapper.class);

    public static TimerTask loginLog(final Integer userId, final String ip) {
        return new TimerTask() {
            @Override
            public void run() {
                try {
                    LoginLog loginLog = LogFactory.createLoginLog(LogType.LOGIN, userId, null, ip);
                    loginLogMapper.insert(loginLog);
                } catch (Exception e) {
                    logger.error("创建登录日志异常!", e);
                }
            }
        };
    }
    //省略很多，慢慢研究代码。
}


```
日志管理器
```java
public class LogManager {

    //日志记录操作延时
    private final int OPERATE_DELAY_TIME = 10;

    //异步操作记录日志的线程池
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);

    private LogManager() {
    }

    public static LogManager logManager = new LogManager();

    public static LogManager me() {
        return logManager;
    }

    public void executeLog(TimerTask task) {
        executor.schedule(task, OPERATE_DELAY_TIME, TimeUnit.MILLISECONDS);
    }
}
--------------------------------------------------------------------------------
/**
 * 被修改的bean临时存放的地方
 */
@Component
@Scope(scopeName = WebApplicationContext.SCOPE_SESSION)
public class LogObjectHolder implements Serializable{

    private Object object = null;

    public void set(Object obj) {
        this.object = obj;
    }

    public Object get() {
        return object;
    }
    //这个方法是重点。
    public static LogObjectHolder me(){
        LogObjectHolder bean = SpringContextHolder.getBean(LogObjectHolder.class);
        return bean;
    }
}
------------------------注解----------------------------------------------------
/**
 * 标记需要做业务日志的方法
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface BussinessLog {

    /**
     * 业务的名称,例如:"修改菜单"
     */
    String value() default "";

    /**
     * 被修改的实体的唯一标识,例如:菜单实体的唯一标识为"id"
     */
    String key() default "id";

    /**
     * 字典(用于查找key的中文名称和字段的中文名称)
     */
    String dict() default "SystemDict";
}


```
这是一个切面，
```java
@Aspect
@Component
public class LogAop {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Pointcut(value = "@annotation(com.guo.guns.common.annotion.log.BussinessLog)")
    public void cutService() {
    }

    @Around("cutService()")
    public Object recordSysLog(ProceedingJoinPoint point) throws Throwable {

        //先执行业务
        Object result = point.proceed();

        try {
            handle(point);
        } catch (Exception e) {
            log.error("日志记录出错!", e);
        }

        return result;
    }

    private void handle(ProceedingJoinPoint point) throws Exception {

        //获取拦截的方法名
        Signature sig = point.getSignature();
        MethodSignature msig = null;
        if (!(sig instanceof MethodSignature)) {
            throw new IllegalArgumentException("该注解只能用于方法");
        }
        msig = (MethodSignature) sig;
        Object target = point.getTarget();
        Method currentMethod = target.getClass().getMethod(msig.getName(), msig.getParameterTypes());
        String methodName = currentMethod.getName();

        //如果当前用户未登录，不做日志
        ShiroUser user = ShiroKit.getUser();
        if (null == user) {
            return;
        }

        //获取拦截方法的参数
        String className = point.getTarget().getClass().getName();
        Object[] params = point.getArgs();

        //获取操作名称
        BussinessLog annotation = currentMethod.getAnnotation(BussinessLog.class);
        String bussinessName = annotation.value();
        String key = annotation.key();
        String dictClass = annotation.dict();

        StringBuilder sb = new StringBuilder();
        for (Object param : params) {
            sb.append(param);
            sb.append(" & ");
        }

        //如果涉及到修改,比对变化
        String msg;
        if (bussinessName.indexOf("修改") != -1 || bussinessName.indexOf("编辑") != -1) {
            Object obj1 = LogObjectHolder.me().get();
            Map<String, String> obj2 = HttpKit.getRequestParameters();
            msg = Contrast.contrastObj(dictClass, key, obj1, obj2);
        } else {
            Map<String, String> parameters = HttpKit.getRequestParameters();
            AbstractDictMap dictMap = DictMapFactory.createDictMap(dictClass);
            msg = Contrast.parseMutiKey(dictMap,key,parameters);
        }

        LogManager.me().executeLog(LogTaskFactory.bussinessLog(user.getId(), bussinessName, className, methodName, msg));
    }
}

```

业务逻辑还需好好研究下。这里只是走一个过程，用的时候心里有个印象。真的好想把作者的名字都贴上去，但是地方不允许。这里感谢要[abel533](https://github.com/abel533/guns)和 [stylefeng](https://gitee.com/naan1993)，像大佬学习。

今晚就先到这里吧，下一个是ehcache，前台的jd插件和beet模板引擎留到最后看。gogogo。
