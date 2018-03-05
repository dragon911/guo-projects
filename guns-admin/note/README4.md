### 3、Spring Boot 缓存配置、全局异常处理

## 说明
**如果您有幸能看到，请认阅读以下内容；**
- 1、本项目临摹自[abel533](https://github.com/abel533/guns)的Guns，他的项目 fork 自 [stylefeng](https://gitee.com/naan1993) 的 [Guns](https://git.oschina.net/naan1993/guns)！开源的世界真好，可以学到很多知识。

- 2、版权归原作者所有，自己只是学习使用。跟着大佬的思路，希望自己也能变成大佬。gogogo》。。

- 3、目前只是一个后台模块，希望自己技能增强到一定时，可以把[stylefeng](https://gitee.com/naan1993) 的 [Guns]融合进来。
- 4、note里面是自己的学习过程，菜鸟写的，不是大佬写的。内容都是大佬的。

昨天看来数据源、日志记录纸配置，我们今天再来看看缓存配置。

### 缓存配置

1、利用Ehcache框架对经常调用的查询进行缓存，从而提高系统性能。还是先看接口定义,需要注意的是get()方法使用了泛型<T>.
```java
/**
 * 通用缓存接口
 */
public interface ICache {

	void put(String cacheName, Object key, Object value);

	<T> T get(String cacheName, Object key);

	@SuppressWarnings("rawtypes")
	List getKeys(String cacheName);

	void remove(String cacheName, Object key);

	void removeAll(String cacheName);

	<T> T get(String cacheName, Object key, ILoader iLoader);

	<T> T get(String cacheName, Object key, Class<? extends ILoader> iLoaderClass);

}
--------------------------------------------------------------------------------
/**
 *  数据重载
 */
public interface ILoader {
	Object load();
}
```

### 抽象类
接下来看下基础CacheFactory，注意，这里定义成抽象的。因为抽象类天生就是用来被继承的。

那什么时候使用抽象类和接口呢：

- 1、如果你拥有一些方法想让他们中的一些默认实现，那么使用抽象类。
- 2、如果你想实现多重继承，那么你必须使用接口。由于java不支多继承，子类不能够继承多个类，但可以实现多个接口
- 3、如果基本功能在不断改变，那么就需要使用抽象类。如果不断改变基本功能并且使用接口 ，那么就需要改变所有实现了该接口的类。

```java
/**
 * 缓存工厂基类
 */
public abstract class BaseCacheFactory implements ICache {

	@SuppressWarnings("unchecked")
	public <T> T get(String cacheName, Object key, ILoader iLoader) {..略..}

	@SuppressWarnings("unchecked")
	public <T> T get(String cacheName, Object key, Class<? extends ILoader> iLoaderClass) {
		Object data = get(cacheName, key);
		if (data == null) {
			try {
				ILoader dataLoader = iLoaderClass.newInstance();
				data = dataLoader.load();
				put(cacheName, key, data);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return (T) data;
	}
}
```
### 延迟初始化方案
接着在看看具体的`EnCacheFactory`,这里你自己也可以定义其他缓存工厂，扩展的时候只要继承`BaseCacheFactory`就行。

第一点需要注意的是这里使用了`org.slf4j.LoggerFactory`

第二点需要注意的是静态`getCacheManager()`方法，这里使用了双重检查机制，还有延时加载(创建)。有没有想起单例模式啊，直接贴一段代码

关键点是使用了`volatile`和`synchronized`保证了可见性和同步性。后者可以用在方法上，代码块上，具体内容看这里吧，不展开了[友情提示](http://www.infoq.com/cn/articles/double-checked-locking-with-delay-initialization).

主要作用：延迟初始化降低了初始化类或创建实例的开销，但也增加了访问被延迟初始化的字段的开销。正常初始化要优于延迟加载，

如果确实要对实例字段使用多线程的安全的延迟初始化，使用基于volatile的初始化，如果需要对静态字段使用线程安全的初始化，则使用基于类的初始化方案。

```java
/**
 * Created by guo on 2018/1/29.
 */
public class SafeDoubleCheckedLocking {
    private volatile static Instacen instance;
    public static Instacen getInstance() {
        if(instance == null) {
            synchronized (SafeDoubleCheckedLocking.class) {
                if (instance == null) {
                    instance = new Instacen();
                }
            }
        }
        return instance;
    }
}
class Instacen{

}
--------------------------对比-------------------------------------------------
/**
 * Created by guo on 2018/1/29.
 * 基于类的初始化解决方案
 */
public class InstanceFactory {
    private static class InstanceHolder{
        public static Instance instance = new Instance();
    }
    public static Instacen getInstance() {
        return InstanceHolder.instance;
    }
}
class Instance extends Instacen {

}

```
回到我们Ehcache缓存工厂吧，重点是`CacheManager`.Spring框架底层有许多个Manager。如`DataSourceTransactionManager`.还有就是创建CacheManager的`create()`方法。人家也使用了双重检查，延迟加载。看见**singleton**了么。`private static volatile CacheManager singleton;`

```java
public static CacheManager create() throws CacheException {
    if(singleton != null) {
        LOG.debug("Attempting to create an existing singleton. Existing singleton returned.");
        return singleton;
    } else {
        Class var0 = CacheManager.class;
        synchronized(CacheManager.class) {
            if(singleton == null) {
                singleton = newInstance();
            } else {
                LOG.debug("Attempting to create an existing singleton. Existing singleton returned.");
            }

            return singleton;
        }
    }
}
```
这里是调用`cacheManager.getCache`来获取缓存。大家还是亲自看看源码把，这里只是自己明白了讨论，记录下。
```java
/**
 * Ehcache缓存工厂
 */
public class EhcacheFactory extends BaseCacheFactory {

	private static CacheManager cacheManager;
	private static volatile Object locker = new Object();
	private static final Logger log = LoggerFactory.getLogger(EhcacheFactory.class);

	private static CacheManager getCacheManager() {
		if (cacheManager == null) {
			synchronized (EhcacheFactory.class) {
				if (cacheManager == null) {
					cacheManager = CacheManager.create();
				}
			}
		}
		return cacheManager;
	}

	static Cache getOrAddCache(String cacheName) {
		CacheManager cacheManager = getCacheManager();
		Cache cache = cacheManager.getCache(cacheName);
		if (cache == null) {
			synchronized(locker) {
				cache = cacheManager.getCache(cacheName);
				if (cache == null) {
					log.warn("无法找到缓存 [" + cacheName + "]的配置, 使用默认配置.");
					cacheManager.addCacheIfAbsent(cacheName);
					cache = cacheManager.getCache(cacheName);
					log.debug("缓存 [" + cacheName + "] 启动.");
				}
			}
		}
		return cache;
	}
-----------------------省略几个----------------------------------------------
	public void put(String cacheName, Object key, Object value) {
		getOrAddCache(cacheName).put(new Element(key, value));
	}

	public <T> T get(String cacheName, Object key) {
		Element element = getOrAddCache(cacheName).get(key);
		return element != null ? (T)element.getObjectValue() : null;
	}
	public void remove(String cacheName, Object key) {
		getOrAddCache(cacheName).remove(key);
	}
}
```

接着我们来看几个常量的定义及实现

```java
/**
 * 获取被缓存的对象(用户删除业务)
 */
String getCacheObject(String para);
-----------------------------------------------------------------------------------
/**
 * 获取被缓存的对象(用户删除业务)
 */
@Override
public String getCacheObject(String para) {
    return LogObjectHolder.me().get().toString();     //还有一个set()记得吗？
}
```
配置完了你总的使用，看代码。先不关注权限那块。`CacheKit`是一个工具类。
```java
/**
 * 删除角色
 */
@RequestMapping(value = "/remove")
@BussinessLog(value = "删除角色", key = "roleId", dict = Dict.DeleteDict)
@Permission(Const.ADMIN_NAME)
@ResponseBody
public Tip remove(@RequestParam Integer roleId) {
    if (ToolUtil.isEmpty(roleId)) {
        throw new BussinessException(BizExceptionEnum.REQUEST_NULL);
    }
    //不能删除超级管理员角色
    if(roleId.equals(Const.ADMIN_ROLE_ID)){
        throw new BussinessException(BizExceptionEnum.CANT_DELETE_ADMIN);
    }
    //缓存被删除的角色名称
    LogObjectHolder.me().set(ConstantFactory.me().getSingleRoleName(roleId));
    roleService.delRoleById(roleId);
    //删除缓存
    CacheKit.removeAll(Cache.CONSTANT);
    return SUCCESS_TIP;
}

---------------------------工具类------------------------------------------------
/**
 * 缓存工具类
 */
public class CacheKit {
	private static ICache defaultCacheFactory = new EhcacheFactory();    //这里创建Encache工厂。
	public static void put(String cacheName, Object key, Object value) {
		defaultCacheFactory.put(cacheName, key, value);
	}
	public static void removeAll(String cacheName) {
		defaultCacheFactory.removeAll(cacheName);
	}
}
```
到这里缓存部分算是结束了，再次说明，只是自己记录过程，要让我实现，目前不现实，还需要自己请自看看源码，跑一遍。

### 全局异常处理

接下来，我们在看看控制统一的异常拦截机制。这里用到了切面的思想。第一眼看到的是@ControllerAdvice。这是什么东东，看图说话。`GlobalExceptionHandler`全部代码点这里[点这里](https://gist.github.com/guoxiaoxu/9157c82d828968a6e0dce217ef931110)。ResponseStatus状态先不关注。

![](https://i.imgur.com/IZvSXiM.jpg)

![](https://i.imgur.com/iuuRMVm.jpg)

```java
/**
 * 全局的的异常拦截器（拦截所有的控制器）（带有@RequestMapping注解的方法上都会拦截）
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * 拦截业务异常
     *
     * @author fengshuonan
     */
    @ExceptionHandler(BussinessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorTip notFount(BussinessException e) {
        LogManager.me().executeLog(LogTaskFactory.exceptionLog(ShiroKit.getUser().getId(), e));
        getRequest().setAttribute("tip", e.getMessage());
        log.error("业务异常:", e);
        return new ErrorTip(e.getCode(), e.getMessage());
    }

    /**
     * 用户未登录
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String unAuth(AuthenticationException e) {
        log.error("用户未登陆：", e);
        return "/login.html";
    }


    /**
     * 拦截未知的运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorTip notFount(RuntimeException e) {
        LogManager.me().executeLog(LogTaskFactory.exceptionLog(ShiroKit.getUser().getId(), e));
        getRequest().setAttribute("tip", "服务器未知运行时异常");
        log.error("运行时异常:", e);
        return new ErrorTip(BizExceptionEnum.SERVER_ERROR);
    }
}
```

异常处理看得也差不多了，接下来看什么好呢？持续关注，



































































































































































-
