### 6、无标题，

Beetl是Bee Template Language的缩写，它绝不是简单的另外一种模板引擎，而是新一代的模板引擎，它功能强大，性能良好，超过当前流行的模板引擎。而且还易学易用。

Beetl类似Javascript语法和习俗，只要半小时就能通过半学半猜完全掌握用法。拒绝其他模板引擎那种非人性化的语法和习俗。同时也能支持html 标签，使得开发CMS系统比较容易

Beetl能很容易的与各种web框架整合，如Act Framework，Spring MVC，Struts，Nutz，Jodd，Servlet，JFinal等。支持模板单独开发和测试，即在MVC架构中，即使没有M和C部分，也能开发和测试模板

Beetl远超过主流java模板引擎性能(引擎性能5-6倍与freemaker，2倍于JSP)，宏观上通过了优化的渲染引擎，IO的二进制输出，字节码属性访问增强，微观上通过一维数组保存上下文Context,静态文本合并处理，重复使用字节数组来防止java频繁的创建和销毁数组，还使用模板缓存，运行时优化等方法。

...

其他模板引擎如Velocity、FreeMake、Thymeleaf。Thymeleaf模板是原始的，不依赖于标签，它能在接受原始HTML的地方进行编辑和渲染。还有一点就是没有与Servlet耦合，只需要有个浏览器就可以进行开发。个人感觉Thymeleaf非常不错。

我在思考今天的主角是谁？思来想去，还是Thymeleaf吧。既然大佬的项目中使用了Beetl那就先看一下他的介绍。


### beetl对前台页面的拆分与包装

例如，把主页拆分成三部分，每个部分单独一个页面，更加便于维护

```xml
<!--左侧导航开始-->
    @include("/common/_tab.html"){}
<!--左侧导航结束-->

<!--右侧部分开始-->
    @include("/common/_right.html"){}
<!--右侧部分结束-->

<!--右侧边栏开始-->
    @include("/common/_theme.html"){}
<!--右侧边栏结束-->
```

以及对重复的html进行包装，使前端页面更加专注于业务实现，例如,把所有页面引用包进行提取
```xml
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="renderer" content="webkit" /><!-- 让360浏览器默认选择webkit内核 -->

<!-- 全局css -->
<link rel="shortcut icon" href="${ctxPath}/static/favicon.ico">
<!-- 全局js -->
<script src="${ctxPath}/static/js/jquery.min.js?v=2.1.4"></script>
<body class="gray-bg">
	<div class="wrapper wrapper-content animated fadeInRight">
		${layoutContent}
	</div>
	<script src="${ctxPath}/static/js/content.js?v=1.0.0"></script>
</body>
</html>
```

开发页面时，只需编写如下代码即可
```xml
@layout("/common/_container.html"){
<div class="row">
    <div class="col-sm-12">
        <div class="ibox float-e-margins">
            <div class="ibox-title">
                <h5>部门管理</h5>
            </div>
            <div class="ibox-content">
               //自定义内容
            </div>
        </div>
    </div>
</div>
<script src="${ctxPath}/static/modular/system/dept/dept.js"></script>
@}
```
以上beetl的用法请参考beetl说明文档。

以上内容来自beetl示例来自[abel533大佬的README](https://github.com/abel533/guns)

为了偷懒，直接粘笔记。


## 6.1 理解视图解析

在上一章中，我们所编写的控制器方法都是没有直接产生浏览器中渲染所需的HTML，这些方法只是将一些数据填充到模型中。然后将模型传递给一个用来渲染的视图。这些方法会返回一个String类型的值，这个值是视图的逻辑名称，不会直接引用具体的视图实现。

**将控制器中请求处理的逻辑视图和视图中的渲染实现解耦是Spirng MVC 的一个重要特性。** 如果控制器中的方法直接负责产生HTML的话，就很难在不影响请求处理的逻辑前提下，维护和更新视图。控制器方法和视图的实现会在模型内容上达成一致，这是两者的最大关联，除此之外，两者应该保持足够的距离。**说直白点就是做好你自己该做的事情,不要今天学那个，明天又学那个**

但是，如果控制器只是通过逻辑视图名来了解视图的话，那Spring该如何确定使用哪一个视图实现来渲染模型呢？这就是Spirng视图解析器的任务了。

在上一章中，使用InternalResourceViewResolver的视图解析器。在它的配置中，为了得到视图的名字，会使用"/WEB-INF/view"前缀和“.jsp”后缀，从而确定来渲染模型的JSP文件的物理位置。

Spring MVC 定义了一个名为ViewResolver的接口，它大概如下所示。
```java
public interface ViewResolver {

	View resolveViewName(String viewName, Locale locale) throws Exception;

}
```

当给`resolveViewName()`方法传入一个视图名和Locale对象时，它会返回一个View实例，View是另外一个接口

```java
public interface View {

	/**
	 * Return the content type of the view, if predetermined.
	 */
	String getContentType();

	/**
	 * Render the view given the specified model.
	 */
	void render(Map<String, ?> model,
          HttpServletRequest request,
          HttpServletResponse response) throws Exception;
}
```
View接口的任务就是接受模型以及Servlet的request和response对象，并将输出结果渲染到response中。

在这里提及的这些接口只是为了让你对视图解析内部有一定的了解。Spring提供了多个内置的实现

- BeanNameViewResolver ：  将视图解析为Spirng应用上下文中的bean，其中bean的Id与视图名字相同
- FreeMarkerViewResolver： 将视图解析为FreeMarker模板
- InternalResourceViewResolver：将视图解析为Web应用的内部资源(一般为JSP)
- VelocityViewResolver：将视图解析为Velocity模板
- XmlViewResolver：将视图解析为特定XMl文件中bean定义。
- TilesViewResolver：将视图解析为Apache Tile定义，其中tile ID和视图名称相同
- ResourceBundleViewResolve：将视图解析为资源bundle(一般为属性文件)

Spring 4 和Spring3.2 支持上面所有的视图解析器

Thymeleaf是一种用来代替JSP的新兴技术，Spring提供了与Thymeleaf的原生模板(natural template)协作的视图解析器，这种模板之所以得到这样的称呼是因为它更将是最终产生的HTML，而不是驱动它们的Java代码。

JSP曾经是，而且现在依然还是Java领域占主导地位的视图技术。

## 6.2 创建JSP视图

不管你是否相信，JavaServer Pages作为Java Web应用程序的视图技术已经超过19年？尽管开始的时候它很丑陋，只是类似模板技术的Java版本，但JSP这些年在不断进化，包含了对表达式语言和自定义标签库的支持。

Spring提供了两种支持JSP视图的方式：
- InternalResourceViewResolver会将视图名解析为JSP文件。如果使用了JSP标签库(JSTL)的话会将视图解析为JstlView形式的jsp文件,从而将JSTL本地化和资源bundle变量暴露给JSTL的格式化和信息标签。
- Spring提供了两个JSP标签库，一个用于表单到模板的绑定，另一个提供了通用的工具类特性。

不管你是用哪种，配置解析JSP的视图解析器是非常重要的。`InternalResourceVIewResolver`是最简单个最常用的视图解析器.

### 6.2.1 配置适用于JSP的视图解析器。
有一些视图解析器，如`ResourceBundleViewResolve`会直接将逻辑视图名映射为特定的View接口实现，而`InternalResourceVIewResolver`所采用的方式不那么直接，它遵循一种约定，会在视图名上添加前缀和后缀，进而确定一个Web应用中视图资源的路径。

假设逻辑视图名为home，通用的实践是将JSP文件放到应用的WEB-INF目录下，防止对他直接访问。物理视图的路径是`/WEB-INF/view/home.jsp`

当使用@Bean注解的时候，我们可以按照如下的方式配置`InternalResourceVIewResolver`,使其在解析视图时，遵循上述约定。
```java
@Bean
public ViewResolver viewResolver() {
  InternalResourceVIewResolver resolver = new InternalResourceVIewResolver();
  resolver.setPrefix("WEB-INF/view");
  resolver.setSuffix(".jsp")
  return resolver;
}
```

作为替代方法，如果你更喜欢基于XML的Spring配置，那么可以按照如下方式配置`InternalResourceVIewResolver`

```xml
<bean id="viewResolver"
  class="org.springframework.web.servlet.view.InternalResourceVIewResolver"
  p:prefix="/WEB-INF/view"
  p:suffix=".jsp"
```
当逻辑视图名中包含斜线时，这个斜线也会带到资源的路径名中。

解析JSTL

到目前为止，我们对`InternalResourceVIewResolver`的配置都很基础很简单。它最终会将逻辑视图名解析为`InternalResourceView`实例，这里实例会引用JSP文件，但是，如果这些JPS使用JSTL标签来处理格式化和信息的话，那么我们会希望`InternalResourceVIewResolver`将视图解析为JstlView。

JSTL的格式化标签需要一个locale对象，以便恰当的格式化地域相关的值，如日期和货币。信息标签可以借助于Spirng的信息资源和Localae，从而选择适当的信息渲染到HTML之中，通过解析JstlView，JSTL能够获得Locale对象以及Spring中配置的信息资源。

如果你想让`InternalResourceVIewResolver`将视图解析为JstlView，而不是`InternalResourceView`的话，那么我们只需要设置它的viewClass属性即可：

```java
@Bean
public ViewResolver viewResolver() {
  InternalResourceVIewResolver resolver = new InternalResourceVIewResolver();
  resolver.setPrefix("WEB-INF/view");
  resolver.setSuffix(".jsp")
  resolver.setViewClass("org.springframework.web.servlet.view.JstlView.class")
  return resolver;
}
```

同样，我们也可以在XMl完成这一任务
```xml
<bean id="viewResolver"
  class="org.springframework.web.servlet.view.InternalResourceVIewResolver"
  p:prefix="/WEB-INF/view"
  p:suffix=".jsp"
  p:viewClass="org.springframework.web.servlet.view.JstlView.class"
```

不管使用Java配置还是使用XML配置，都能确保JSTL的格式化和信息标签能够获得Locale对象以及Spring中配置的信息资源。

### 6.2.2 使用Spring的JSP库

当为JSP添加功能时，标签库是一种强大的方式，能够避免在脚本块中直接编写Java代码。

Spring提供了两个标签库，一个用来渲染HTML表单标签，可以绑定model中的某个属性，另一个标签包含了一些工具类标签。其中表单标签最有用。**如何将Spittr应用的注册表单绑定到模型上，这样表单就可以预先填充值，并在在表单提交失败后，能够展现校验错误**

将表单绑定到模型上

Spring的表单绑定JSP标签库包含了14个标签，他们中的大多数都用来渲染HTML中的表单标签。与原生的区别在于它会绑定模型中的一个对象，能够根据模型中对象的属性填充值。标签库中还包含了一个为用户展现错误的标签，它会将错误信息渲染到最终的HTML之中。

为了使用表单绑定库，需要在JSP页面中对其进行声明

```xml
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf" %>
```

需要注意的是，我将前缀指定为“sf”，但通常也可能使用"form"前缀，也可以任意选择。

声明完表单绑定标签库之后，你就可以使用14个相关的标签了
- <sf:checkbox> :
- <sf:checkboxes> :
- <sf:errors>
- <sf:form>
- <sf:hidden>
- <sf:input>
- <sf:table>
- <sf:option>
- <sf:password>
- <sf:select>
- <sf:textarea>
- <sf:radilbutton>

就Spittr样例来数，只用到适合于Spittr应用中注册表单的标签

```xml
<sf:form method="POST" comandName="spitter">
  First Name: <sf:input path="firstName" /><br/>
  Last Name: <sf:input path="lastName" /><br/>
  Email: <sf:input path="email" /><br/>
  Username: <sf:input path="username" /><br/>
  Password: <sf:password path="password" /><br/>
  <sf:input type="submit" value="Register" />
</sf:form>
```
<sf:form>会渲染一个HTMl<form>标签，**但它也会通过commandName属性构建针对某个模型对象的上下文信息，在其他的表单绑定标签中，会引用这个模型对象的属性**

因此在模型中必须要有一个key为Spitter对象，否则的话，表单不能正常渲染(会出现JSP错误).这意味着我们需要修改SpitterController，以确保模型中存在以Spitter为key的Spitter对象。

```java
@RequestMapping(value = "/register", method = RequestMethod.GET)
public String showRegistrationForm(Model model) {
    model.addAttribute(new Spitter());
    return "registerForm";
}
```

新增了一个Spitter实例到模型中，模型中的key根据对象类型推断的，也就是spitter，与我们所需的一致。


我们在这里设置了path属性，** < input>标签的value属性值将会设置为spitter对象中path属性所对应的值** 例如在模型中Spitter对象中firstName属性值为guo，那么<sf:input path="firstname"/>所渲染的< input>标签中，会存在value=“guo”。

<sf:password>它的值不会直接明文显示。

从Spring3.1开始<sf:form>标签能够允许我们指定type属性，这样的话，除了其他可选的类型外，还能指定HTML5特定类型的文本域，如date、range、email。我们可以按照如下方式指定email域：
```xml
Email: <sf:input path="email" type="email"/> <br/>
```

为了指导用户矫正错误，我们需要使用<sf:errors>

**展示错误**

如果存在校验错误的话，请求中会包含错误的详细信息，这些信息是与模型数据放到一起的。我们所需要做的就是到模型中将这些数据抽取出来，并展现给用户，<sf:errors>可以让这项任务变得简单。

例如，我们将<sf:errors>用到registerForm.jsp中的代码片段
```xml
<sf:form method="POST" commandName="spitter" >
    <sf:input path="firstName" cssErrorClass="error" /><br/>
    <sf:errors path="firstName" />
    ......
</sf:form>
```

尽管我只展现了将<sf;errors>用到了firstname输入域的场景，其他域也一样

默认情况下，错误会渲染在一个HTML<span>标签中，如果只是显示一个错误的话，这是不错的选择，但是如果要渲染所有的错误，很可能不止一个错误，所以使用像<div>这样的块级元素更为合适。因此，我们可以将element属性设置为div，这样的话，错误就会渲染到一个<div>中了。

```xml
<sf:form method="POST" commandName="spitter" >
    <sf:errors path="*" element="div" cssClass="errors" />
    <sf:label path="firstName"
              cssErrorClass="error">First Name</sf:label>:
    <sf:input path="firstName" cssErrorClass="error" /><br/>
    <sf:label path="lastName"
              cssErrorClass="error">Last Name</sf:label>:
    <sf:input path="lastName" cssErrorClass="error" /><br/>
    <sf:label path="email"
              cssErrorClass="error">Email</sf:label>:
    <sf:input path="email" cssErrorClass="error" /><br/>
    <sf:label path="username"
              cssErrorClass="error">Username</sf:label>:
    <sf:input path="username" cssErrorClass="error" /><br/>
    <sf:label path="password"
              cssErrorClass="error">Password</sf:label>:
    <sf:password path="password" cssErrorClass="error" /><br/>
    <input type="submit" value="Register" />
</sf:form>
```

<sf:label>标签 像其他的表单绑定标签一样，使用path来指定它属于模型对象中哪个属性，假设没有校验错误的话，它将会渲染为HTML<label>

------
省略...具体的内容请看书，

---------
```java
@NotNull
@Size(min=5, max=16, message="{username.size}")
private String username;
.......
@NotNull
@Size(min=2, max=30, message="{lastName.size}")
private String lastName;
```

我们将其@Size注解的 message设置为一个字符串。大括号括起来的。没有没有大括号的话，message中的值将会作为展现给用户的错误信息，使用了就用文件中某个一个属性，该属性包含了实际的信息。

创建一个ValidationMessages.properties的文件，并将其放到根路径之下。
```xml
firstName.size=First name must be between {min} and {max} characters long.
lastName.size=Last name must be between {min} and {max} characters long.
username.size=Username must be between {min} and {max} characters long.
password.size=Password must be between {min} and {max} characters long.
email.valid=The email address must be valid.
```

将这些错误信息抽取到属性文件中还会带来一个好处，那就是我们可以通过创建地域文件相关的属性文件，为用户展现特定语言和地域的信息。

** Spring通用的标签库 **

要使用Spirng通用的标签库，需要在页面首页对其进行声明
```xml
<%@ taglib uri="http://www.springframework.org.tags" prefix='s'%>
```

也可以任意，声明之后可以使用如下JSP标签：
- <s:bind> :将绑定的属性导出到一个名为status的页面作用域中，与<s:path>组合使用获取绑定的属性
- <s:escapeBody>:将标签中的内容进行HTML和JavaScrop转义
- <s:message>:根基给定的编码获取信息，然后进行渲染，要么将其设置为页面作用域、请求、会话作用域或应用作用域的变量
- <s:theme>:根据给定的编码获取主题信息，然后要么进行渲染。。。。
- <s:transform>:使用命令对象的属性便阿基器转换命令对象中不包含的属性
- <s:url>:创建相对于上下文的URL
- <s:eval>:计算符合Spring表达式语言语法的某个表达式的值，然后进行渲染，要么。。。。
- <s:param>：


展现国际化信息

Web是全球性和网络，你所构建的应用很可能会有全球化用户。

我们可以把文本放到一个或多个属性文件中。借助<s:message>.
```xml
<h1><s:message code="spittr.welcome"/></h1>
```
按照这里的方式，<s:message>将会根据key为spittr。welcome的信息源来渲染文本。需要完成任务，还需要配置这样一个信息源。

Spring 中有多个信息源的类，它们都实现了`MessageSource`接口,更为常见的是`ResourceBundleMessageSource`.他从一个文件中加载信息，这个属性文件的名称是根据基础名称衍生而来的。如下@Bean方法配置类`ResourceBundleMessageSource`

```java
@Bean
public MessageSource messageSource () {
    ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
    messageSource.setBasename("messages");
    messageSource.setCacheSeconds(10);
    return messageSource;
}
```

```xml
spitter.welcome=Welcome to Spitter!
```

在这个Bean声明中，核心在basename属性，将其设置为messages后，`ResourceBundle-MessageSource`就会视图在类路径的属性文件中解析信息，

还有一种是设置到外部。

**创建URL**

<s:utl>是一个很小的标签，主要任务就是创建URL，然后将其赋值给一个变量或者渲染到相应中，它是JSTL中<c:url>标签的替代者。

按其最简单的形式<s:url>会接受一个相对于Servlet上下文的URL，并在进行渲染的时候，预先加上Servlet上下文路径。
```xml
<a href="<s:url href="/spitter/register"/>Register</a>
```

另外还可以使用<s:url>创建URL，并将其赋值给一个变量模板在稍后使用
```xml
<s:url href="/siptter/register" var="registerUrl"/>
<a href="${registerUrl}"/>Register</a>
```

默认情况下，URL是在页面作用域中创建的，但是通过设置scope属性，我们可以让<s:url>在应用作用内、会话作用域或请求作用域内创建URL
```xml
<s:url href="/siptter/register" var="registerUrl" scope="request"/>
<a href="${registerUrl}"/>Register</a>
```
如果需要添加参数可以使用<s:param>标签。

```xml
<s:url href="/siptter/register" var="registerUrl" scope="request">
  <s:param name="max" value="60" />
  <s:param name="min" value="20" />
</s:url>
```
如果你希望将渲染得到的URL内同展现在Web页面上，那么你应该要求<s:url>进行转义，这需要将htmlEscape属性设置为true。  如果是在JavaScript中，需要设置 javaScript-Escape属性设置为true。

到目前为止，我们还没有看到<s:url>能够实现，而JSTL的<c:url>无法实现的功能，

<s:escapeBody>是一个通用的转义标签，它会渲染标签内联的内容，并且在必要时候进行转义。只完成一件事，并且完成的非常好，与<s:url>不同，它只会渲染内容，并不能将内容设置为变量。

  接下来借助Apche Tiles 为模板实现一些通用可重复的布局。

## 6.3 使用Apache Tiles 视图定义布局。

假设我们想为应用中的所有页面定义一个通用的头部和底部，最原始的方式就是查找每个JSP模板，并为其添加头部和底部的HTML。但是这种方法的扩展性不好，也难以维护。为每个页面添加这些元素会有一些初始成本，而后续的每次变更更会消耗类似的成本。

更好的方式是使用局部引擎，如Apache Tiles，定义适用于所有页面的通用页面布局。Spring MVC以视图解析器的形式为Apache Tiles 提供了支持，这个视图解析器能够将逻辑视图名解析为Tile定义。

### 6.3.1 配置Tiles视图解析器

需要配置一个TilesConfigurer bean，它会负责定位和加载Tile定义并协调生成的Tiles。除此之外还需要TilesViewResolver bean 将逻辑视图名解析为Tile定义。

包名不同

首先配置TilesConfigurer来解析Tile定义

```java
//Tile
 @Bean
 public TilesConfigurer tilesConfigurer() {
     TilesConfigurer tiles = new TilesConfigurer();
     tiles.setDefinitions(new String[] {
             "WEB-INF/layout/tiles.xml",
             "/WEB-INF/view/**.tiles.xml"});       //指定Tile定义的位置
     tiles.setCheckRefresh(true);                 //启用刷新功能
     return tiles;
 }

 @Bean
 public ViewResolver viewResolver() {
     return new TilesViewResolver();
 }
```

本例中，使用了Ant风格的通配符，（**）,所以TilesConfigurer会遍历“WEB-INF/”的所有子目录在查找Tile定义。

如果你更喜欢XMl配置的话，那么可以按照如下的形式配置`TilesConfigurer`和`TilesViewResolver`:
```xml
<bea id="tilesConfigurer"
      class="org.springframework.web.servlet.view.tiles3.TilesConfigurer">
    <property name="difinitions">
      <list>
        <value>WEB-INF/layout/tiles.xml</value>
        <value>/WEB-INF/view/**.tiles.xml</value>
    </property>
</bean>
<bean id="ViewResolver"
    class="org.springframework.web.servlet.view.tiles3.TilesViewResolver" />
```

`TilesConfigurer`会加载Tiles定义并与`Apache Tiles`协作，而`TilesViewResolver`会将逻辑视图名解析为引用Tiles定义的视图，它是通过查找与逻辑视图名称想匹配的Tiles定义实现该功能的。需要定义几个Tile定义以了解它是如何运转的

** 定义Tiles **

Apache Tiles 提供了一个文档类型定义(docment type definition,DTD) 用来在XML文件中指定Tile的定义。每个定义中需要包含一个<definition>元素，这个元素会有一个或多个<put-attribute>元素。

```xml
<?xml version="1.0" encoding="ISO-8859-1" ?>
<!DOCTYPE tiles-definitions PUBLIC
       "-//Apache Software Foundation//DTD Tiles Configuration 3.0//EN"
       "http://tiles.apache.org/dtds/tiles-config_3_0.dtd">
<tiles-definitions>

  <definition name="base" template="/WEB-INF/layout/page.jsp">           <!--定义base Tile-->
    <put-attribute name="header" value="/WEB-INF/layout/header.jsp" />
    <put-attribute name="footer" value="/WEB-INF/layout/footer.jsp" />     <!--设置属性-->
  </definition>

  <definition name="home" extends="base">                                   <!--扩展base Tile-->
    <put-attribute name="body" value="/WEB-INF/views/home.jsp" />
  </definition>

  <definition name="registerForm" extends="base">
    <put-attribute name="body" value="/WEB-INF/views/registerForm.jsp" />
  </definition>

  <definition name="profile" extends="base">
    <put-attribute name="body" value="/WEB-INF/views/profile.jsp" />
  </definition>

  <definition name="spittles" extends="base">
    <put-attribute name="body" value="/WEB-INF/views/spittles.jsp" />
  </definition>

  <definition name="spittle" extends="base">
    <put-attribute name="body" value="/WEB-INF/views/spittle.jsp" />
  </definition>

</tiles-definitions>
```
每个<difinition>元素定义了一个Tile，它最终引用的是一个JSP模板。对于base Tiles来讲，它引用的是一个头部JSP模板和一个底部JSP模板

baseTile所引用的page.jsp模板如下所示：
```xml
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="t" %>
<%@ page session="false" %>
<html>
  <head>
    <title>Spittr</title>
    <link rel="stylesheet"
          type="text/css"
          href="<s:url value="/resources/style.css" />" >
  </head>
  <body>
    <div id="header">
      <t:insertAttribute name="header" />             <%--插入头部--%>
    </div>
    <div id="content">
      <t:insertAttribute name="body" />               <%--插入主题内容--%>
    </div>
    <div id="footer">
      <t:insertAttribute name="footer" />              <%--插入底部--%>
    </div>
  </body>
</html>
```

在以上代码中，重点关注的是事情就是：如何使用Tile标签库中的<t:insertAttribute>JSP标签来插入其他的模板。

属性引用的每个模板是很简单的。

```xml
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<a href="<s:url value="/" />"><img
    src="<s:url value="/resources" />/images/spitter_logo_50.png"
    border="0"/></a>
```

为了完整的了解home Tile 展现如下home.jsp
```xml
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<h1>Welcome to Spitter</h1>

<a href="<c:url value="/spittles" />">Spittles</a> |
<a href="<c:url value="/spitter/register" />">Register</a>
```

这里的关键点在于通用的元素放到了page.jsp、header.jsp以及footer.jsp中，其他的Tile模板中不再包含这部分内容。这使用它们能够跨页面重用，这些元素的维护也得以简化。

![](https://i.imgur.com/0KCTGa0.jpg)

为了展示这张图，费力九牛二虎之力。得到了一点经验：要按照作者的套路出牌，别着急瞎捣腾。。 只怪：`<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>`报错！！！ 想给大家分享下我的书签，太多有用了的了。需要的留言，我放项目目录下。

在Java Web应用领域中，JSP长期以来都是占据主导地位的方案，但是这个领域有了新的竞争者，也就是Thymeleaf。

## 6.4 使用Thymeleaf

尽管JSP已经存在了很长时间，并且在JavaWeb服务器中无处不在，但是，它却存在一些缺陷。JSP最明显的问题在于它看起来像HTML或XML，但它其实不是。大多数的JSP模板都是采用HTML的形式，但是又参杂了各种JSP标签库的标签，使其变得混乱。这些标签库能够以很便利的方式为JSP带来动态渲染的强大功能，但是它也摧毁了我们想维持一个格式良好的文档的可能性，作为一个极端的样例，如下的JSP标签甚至作为HTML参数的值
```xml
<input type="text" value="<c:out value="${thing.name}"/>"/>
```
标签库和JSP缺乏良好的格式的一个副作用就是它很少能够与其产生的HTML类似。 因为JSP并不是真正的HTML，很多浏览器和编辑器展现的效果都很难在审美上接近模板最终所渲染出来的效果。

同时JSP规范与Servlet规范紧密耦合在一起。这意味着它只能用在基于Servlet的Web应用之中，JSP模板不能呢个作为通用的模板(如格式化Email)，也不能用于非Servlet的Web应用。

最新的挑战者是Thymaleaf，它展现了一些切实的承诺，是一项很令人兴奋的可选方案，Thymeleaf模板是原生的，不依赖于标签库，它能在接受原始HTML的地方进行编辑和渲染。因为它没有和Servlet规范耦合，因此Thymeleaf模板能够进入JSP所无法涉及的领域。让我们看下如何在Spring MVC中使用Thymeleaf。

### 6.4.1 配置Thymeleaf视图解析器

为了要在Sping中使用Thymeleaf，我们需要配置三个启用Thymeleaf与Spirng集成的bean
- ThymeleafViewResolver：将逻辑视图名称解析为Thymeleaf模板视图
- SpringTemplateEngine：处理模板并渲染结果
- TempalateResolver：加载Thymeleaf模板


```java

@Bean
public ViewResolver viewResolver(SpringTemplateEngine templateEngine) {
		ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
		viewResolver.setTemplateEngine(templateEngine);
		return viewResolver;
}
@Bean
public TemplateEngine templateEngine(TemplateResolver templateResolver) {
		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.setTemplateResolver(templateResolver);
		return templateEngine;
}

@Bean
public TemplateResolver templateResolver() {                   //模板解析器
		TemplateResolver templateResolver = new ServletContextTemplateResolver();
		templateResolver.setPrefix("/WEB-INF/templates");
		templateResolver.setSuffix(".html");
		templateResolver.setTemplateMode("THML5");
		return templateResolver;
}
```

使用XMl来配置bean

```xml
<bean id="viewResolver" class="org.thymeleaf.spring4.view.ThymeleafViewResolver"
	p:templateEngine-ref="templateEngine" />
<bean id="templateEngine" class="org.thymeleaf.spring4.SpringTemplateEngine"
	P:templateResolver-ref="templateResolver" />
<bean id="templateResolver" class="org.thymeleaf.templateresolver.ServletContextTemplateResolver"
	p:prefix="WEB-INF/templates/"
	p:suffix=".html"
	p:templateMode="HTML5"
```

不管是使用哪种方式，Thymeleaf已经准备好了，它可以将响应中的模板渲染到Spring MVC控制器所处理的请求中。

ThymeleafViewResolver是Spirng MVC中ViewResolver的一个实现类。像其他视图解析器一样，会接受一个逻辑视图名称，并将其解析为视图。不过在该场景下 ，视图会是一个Thymeleaf模板。

需要注意的是：`ThymeleafViewResolver` bean中注入了一个对`SpringTemplateEngine`bean 的引用。`SpringTemplateEngine`会在Spring中启用Thymeleaf引擎，用来解析模板并基于这些模板渲染结果。

`TemplateResolver`会最终定位和查找模板。与之前配置的`InternalResourceVIewResolver`类似，他使用了prefix 和 suffix属性。它的templateMode属性被设置为HTML5，这表明我们预期要解析的模板会渲染成HTML5输出。

### 6.4.2 定义Thymeleaf模板
Thymeleaf在很大程度上就是HTML文件，与JSP不同，他没有什么特殊的标签或标签库。Thymeleaf之所以能够发挥作用，是因为它通过自定义的命名空间，为标准的HTML标签集合添加Thymeleaf属性。

```html
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:th="http://www.thymeleaf.org">            <!--声明Thymeleaf命名空间-->
  <head>
    <title>Spitter</title>
    <link rel="stylesheet"
          type="text/css"
          th:href="@{/resources/style.css}"></link>    <!--到样式表的th:href链接-->
  </head>
  <body>
      <h1>Welcome to Spitter</h1>
      <a th:href="@{/spittles}">Spittles</a> |          <!--到页面的th:herf链接-->
      <a th:href="@{/spitter/register}">Register</a>
  </body>
</html>
```

首页模板相对简单，只是用来th:href属性，特殊之处在于可以包含Thymeleaf表达式，用来动态计算值。在本例中，使用th:href属性的三个地方都是用到了“@{}”表达式，用来计算相对于URL的路径，(在JSP中，会使用JSTL的<c:url>标签或Spring<s:url>标签类似)。

这意味着Thymeleaf模板与JSP不同，**它能按照原始的方式进行编辑甚至渲染，而不必经过任何类型的处理器**,当然我们需要Thymeleaf来处理模板，并渲染得到最终期望的输出。

**借助Thymeleaf实现表单绑定**

表单绑定是Spring MVC的一项重要特性。它能够将表单提交的数据填充到命令对象中，并将其传递给控制器，而在展现表单的时候，表单中也会填充命令对象中的值。

使用Thymeleaf的Spring方言，参考如下的Thymeleaf模板片段

```xml
	<label th:class="${#fields.hasErrors('firstName')}? 'error'">First Name</label>:
		<input type="text" th:field="*{firstName}"
					 th:class="${#fields.hasErrors('firstName')}? 'error'" /><br/>
```

th:class属性会渲染为一个class属性，他的值是根据给给定的表达式计算得到的。在上面的这两个th:class属性中，它会直接检查firstName域有没有校验错误，如果有的话，class属性在渲染时的值为error，如果这个域没有错误的话，将不会渲染class属性。

<input.>标签使用了th:field属性，用来引用后端对象的firstName域，

完整版如下：
```xml
<form method="POST" th:object="${spitter}">
	<div class="errors" th:if="${#fields.hasErrors('*')}">
		<ul>
			<li th:each="err : ${#fields.errors('*')}"
					th:text="${err}">Input is incorrect</li>
		</ul>
	</div>
	<label th:class="${#fields.hasErrors('firstName')}? 'error'">First Name</label>:
		<input type="text" th:field="*{firstName}"
					 th:class="${#fields.hasErrors('firstName')}? 'error'" /><br/>

	<label th:class="${#fields.hasErrors('lastName')}? 'error'">Last Name</label>:
		<input type="text" th:field="*{lastName}"
					 th:class="${#fields.hasErrors('lastName')}? 'error'" /><br/>

	<label th:class="${#fields.hasErrors('email')}? 'error'">Email</label>:
		<input type="text" th:field="*{email}"
					 th:class="${#fields.hasErrors('email')}? 'error'" /><br/>

	<label th:class="${#fields.hasErrors('username')}? 'error'">Username</label>:
		<input type="text" th:field="*{username}"
					 th:class="${#fields.hasErrors('username')}? 'error'" /><br/>

	<label th:class="${#fields.hasErrors('password')}? 'error'">Password</label>:
		<input type="password" th:field="*{password}"
					 th:class="${#fields.hasErrors('password')}? 'error'" /><br/>
	<input type="submit" value="Register" />
</form>
```
需要注意的是我们在表单的顶部也使用了Thymeleaf，它会用来渲染所有的错误。<div>元素使用th:if属性来检查是否有校验错误,如果有的话，会渲染<div>否则的话不会渲染。
```xml
<div class="errors" th:if="${#fields.hasErrors('*')}">
	<ul>
		<li th:each="err : ${#fields.errors('*')}"
				th:text="${err}">Input is incorrect</li>
	</ul>
</div>
```
在<div>中 会使用一个无序的列表来展现每项错误。<li>标签上的th:each属性将会通知Thymeleaf为每项错误都渲染一个<li>,在每次迭代中会将当前错误设置到一个名为err的变量中。

<li>标签还有一个th:text属性，这个命令会通知Thymeleaf计算某一个表达式并将它的值渲染为<li>标签的内容体。实际上的效果就是每项错误对应一个<li>元素，并展现错误文本。

"${}"表达式是变量表达式，**一般来讲，他们是对象图导航语言(Object-Grapg Navigation language OGNL)表达式，但是在使用Spirng的时候，他们是SpEL表达式，在${Spitter}这里 例子中，它会解析为ket为spitter的model属性。**

而对于`*{}`表达式，他们是选择表达式。**变量表达式是基于整个SpEl上下文计算的，而选择表达式是基于某一个选中对象计算的。**在本例的表单中，选中对象就是<form>标签中的th:object属性所设置的对象:模型中的Spitter对象。因此，“*{firsrName}”表达式就会计算为Spitter对象的firstName属性

作者没有细将，我把代码片段贴出来，

```xml
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:th="http://www.thymeleaf.org">
  <head>
    <title>Spitter</title>
    <link rel="stylesheet" type="text/css"
          th:href="@{/resources/style.css}"></link>
  </head>
  <body>
    <div id="header" th:include="page :: header"></div>

    <div id="content">
      <form method="POST" th:object="${spitter}">
      </form>
    </div>
    <div id="footer" th:include="page :: copy"></div>
  </body>
</html>
```

```xml
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:th="http://www.thymeleaf.org">

  <body>

    <div th:fragment="header">
      <a th:href="@{/}">
        <img th:src="@{/resources/images/spitter_logo_50.png}" border="0" /></a>
    </div>

    <div>Content goes here</div>

    <div th:fragment="copy">Copyright &copy; Craig Walls</div>
  </body>

</html>
```
**注意标签th:include 、th:fragment 、th:src 、**

## 6.5 小节(我喜欢的)

处理请求只是Spirng MVC功能的一部分。如果控制器所产生的结果想让人看到，那么 他们产生的模型数据就要渲染到视图中，并展现到用户的Web浏览器中。Spring的视图渲染是很灵活的，并提供了多个内置的可选方案，包括传统的 JavaServer Page以及流行的Apache Tiles布局引擎。

在本章节中，我们首先快速了解了一下Spring所提供的视图解析器和视图解析器可选方案。还深入学习了如何在Spring MVC中使用JSP 和ApacheTiles。

还看到了如何使用Thymeleaf作为Spirng MVC应用的视图层，它被视为JSP的替代方案。Thymeleaf是一项很有吸引力的技术，**因为它能创建原始的模板，这些模板是纯HTML，能像静态HTML那样以原始的方式编写和预览，并且能够在运行时渲染动态模型数据。** 除此之外，**Thymeleaf是与Servlet没有耦合关系的，这样它就能够用在JSP所不能使用的领域中。**

Spittr应用的视图定义完成之后，**我们已经具有了一个虽然微小但是可部署且具有一定功能的Sprinig MVC Web应用。还有一些其他特性需要更新进来，如数据持久化和安全性**，我们会在合适的时候关注这些特性。但现在，这个应用变得有模有样了。
