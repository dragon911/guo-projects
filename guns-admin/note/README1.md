首先我们来看包结构，老样子，我们先从core包开始，然后common，在config。等待，还是先从启动类开始吧。

### 项目包结构说明
```
├─main
│  │
│  ├─java
│  │   │
│  │   ├─com.guo.guns----------------项目主代码(原来的包：com.stylefeng.guns)
│  │   │          │
│  │   │          ├─common----------------项目公用的部分(业务中经常调用的类,例如常量,异常,实体,注解,分页类,节点类)
│  │   │          │
│  │   │          ├─config----------------项目配置代码(例如mybtais-plus配置,ehcache配置等)
│  │   │          │
│  │   │          ├─core----------------项目运行的核心依靠(例如aop日志记录,拦截器,监听器,guns模板引擎,shiro权限检查等)
│  │   │          │
│  │   │          ├─modular----------------项目业务代码
│  │   │          │
│  │   │          ├─GunsApplication类----------------以main方法启动springboot的类
│  │   │          │
│  │   │          └─GunsServletInitializer类----------------用servlet容器启动springboot的核心类
│  │   │
│  │   └─generator----------------mybatis-plus Entity生成器
│  │
│  ├─resources----------------项目资源文件
│  │     │
│  │     ├─gunsTemplate----------------guns代码生成模板
│  │     │
│  │     ├─application.yml----------------springboot项目配置
│  │     │
│  │     └─ehcache.xml----------------ehcache缓存配置
│  │
│  └─webapp----------------web页面和静态资源存放的目录
│
```
注:SpringBoot项目默认不支持将静态资源和模板(web页面)放到webapp目录,但是个人感觉resources目录只放项目的配置更加简洁,所以就将web页面继续放到webapp目录了.

**1、先来看启动类：**
```java
/**
 * SpringBoot方式启动类
 */
@SpringBootApplication
public class GunsApplication extends WebMvcConfigurerAdapter {

    protected final static Logger logger = LoggerFactory.getLogger(GunsApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(GunsApplication.class,args);
        logger.info("GunsApplication is success!");
    }
}
```
需要注意两个点：@SpringBootApplication注解和WebMvcConfigurerAdapter

(1)、1.2版本应该是@Configuretion注解，这个注解表明这个类会处理Spring的常规bean。来自《精通Spring MVC》

(2)、@ComponentScana 它会告诉Spring去哪里查找SPring组件(服务，控制器)，大白话就是bean那。一般我们在控制层的类上会加上@Controller注解，不知道大家有木有配置过XML，难受啊。

(3)、@EnableAutoConfiguration ： 看名字，AutoConfiguration啊，这就是Spring魔力所在，省去很多XXML了，在这里是基于JavaConfig配置的。



```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(
    excludeFilters = {@Filter(
    type = FilterType.CUSTOM,
    classes = {TypeExcludeFilter.class}
), @Filter(
    type = FilterType.CUSTOM,
    classes = {AutoConfigurationExcludeFilter.class}
)}
)
public @interface SpringBootApplication {
    Class<?>[] scanBasePackageClasses() default {};
}
-------------------------------------------------------
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration
public @interface SpringBootConfiguration {
}
```
**2、接下来，我们再看下为毛要继承WebMvcConfigrerApapter类。**

看见Config没，这个也是配置类，它声明了视图解析器、地域解析器、以及静态资源的位置，(想起来没，就是前置，后置)
。

先看一段源码 ————**源码是个好东西**

```java


----------------------InternalResourceViewResolver熟悉吗？-----------------------
@Bean
@ConditionalOnMissingBean
public InternalResourceViewResolver defaultViewResolver() {
    InternalResourceViewResolver resolver = new InternalResourceViewResolver();
    resolver.setPrefix(this.mvcProperties.getView().getPrefix());
    resolver.setSuffix(this.mvcProperties.getView().getSuffix());
    return resolver;
}
---------------------------也是视图解析器，只是返回的是bean-------------------------
@Bean
@ConditionalOnBean({View.class})
@ConditionalOnMissingBean
public BeanNameViewResolver beanNameViewResolver() {
    BeanNameViewResolver resolver = new BeanNameViewResolver();
    resolver.setOrder(2147483637);
    return resolver;
}
-------------------------------地域解析器--------------------------------------------
@Bean
@ConditionalOnMissingBean
@ConditionalOnProperty(
    prefix = "spring.mvc",
    name = {"locale"}
)
public LocaleResolver localeResolver() {
    if(this.mvcProperties.getLocaleResolver() == org.springframework.boot.autoconfigure.web.WebMvcProperties.LocaleResolver.FIXED) {
        return new FixedLocaleResolver(this.mvcProperties.getLocale());
    } else {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(this.mvcProperties.getLocale());
        return localeResolver;
    }
}
```

说了这么多，我们先来看看SpringMVC启动流程，知其所以然的同时也要知其然。

一般来说，初始化 步骤如下：

- 1、初始化SpringMVC的DispatcherServlet
- 2、配置转码过滤器(UTF-8,乱码锅就在设置，还有一个就是在发送信息前，setCharacterEncoding()。),保证能正确转码，为啥啊，因为浏览器发送的是ISO-8859？。
- 3、配置视图解析器，就上面说的那个，返回视图的时候方便定位。
- 4、配置静态资源的位置，
- 5、还有就是配置multipart解析器，主要是为了能上传文件，part单词什么意思？多个-部分
- 6、还需要写错误页面，统一异常处理。

然而，然而有了SpringBoot，统统可以省略，激动吗？兴奋吗？ 我是蛮激动的，尤其第一次运行SpringBoot项目。

上面已经帮我们位置了视图解析器，接下来我们看下DispatcherServlet和multipart

```java
@AutoConfigureOrder(-2147483648)
@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass({DispatcherServlet.class})     //只有对应的类在classpath中有存在时才会注入
@AutoConfigureAfter({EmbeddedServletContainerAutoConfiguration.class})
public class DispatcherServletAutoConfiguration {
    public static final String DEFAULT_DISPATCHER_SERVLET_BEAN_NAME = "dispatcherServlet";    //熟悉吗？DeFAULT，默认的那。
    public static final String DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME = "dispatcherServletRegistration";

    public DispatcherServletAutoConfiguration() {
    }
-------------------------------MultipartResolver-------------------------------
    @Bean
    @ConditionalOnBean({MultipartResolver.class})
    @ConditionalOnMissingBean(
        name = {"multipartResolver"}
    )
    public MultipartResolver multipartResolver(MultipartResolver resolver) {
        return resolver;
    }
}
```

还有还有，错误配置、转码配置、tomcat配置Jetty等等。具体的在这个配置类中`EmbeddedServletContainerAutoConfiguration`,只看ContainerAutofig。我们还是正式进入项目吧。

```java
/**
 * Guns Web程序启动类
 */
public class GunsServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(GunsApplication.class);
    }
}
```

我们点击源码看看SpringBootServletInitializer。其实看名字就可看出是Servlet初始化，熟悉设这个`ApplicationContext`单词吗？应用上下文。很重要的，还有一个叫做BeanFactory，主要有个getBean方法，一般用前者。不懂的可以去看看我临摹别人的简单版Spring框架[点这里](https://github.com/guoxiaoxu/tiny-spring)
```java
public abstract class SpringBootServletInitializer implements WebApplicationInitializer {

    protected WebApplicationContext createRootApplicationContext(ServletContext servletContext) {}


    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder;
    }
}
```

今晚就先到这里吧，明早gogogo。



















































































































-
