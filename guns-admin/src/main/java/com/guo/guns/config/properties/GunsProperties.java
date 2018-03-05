package com.guo.guns.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;

import static com.guo.guns.core.util.ToolUtil.isEmpty;
import static com.guo.guns.core.util.ToolUtil.getTempPath;

/**
 *
 * guns项目配置
 *
 * @Author guo
 * @Date 2018-03-04 20:17
 */
@Component
@ConfigurationProperties(prefix = GunsProperties.PREFIX)
public class GunsProperties {

    public static final String PREFIX = "guns";

    private boolean kaptchaOpen = false;

    private boolean swaggerOpen = false;

    private String fileUploadPath;

    private Boolean haveCreatePath;

    private boolean springSessionOpen = false;

    private Integer sessionInvalidateTime = 30 * 60;     //session关闭失效时间(默认是30分钟）

    private Integer sessionValidationInterval = 15 * 60; //session验证失效时间时间，（默认十五分钟，单位：秒）

   public String getFileUploadPath() {
       //如果没有写文件上传路径，则保存在临时目录中
       if (isEmpty(fileUploadPath))  {
           return getTempPath();
       } else {
           //判断有没有结尾符,没有得加上
           if (!fileUploadPath.endsWith(File.separator)) {
               fileUploadPath = fileUploadPath + File.separator;
           }
           //判断目录存不存在,不存在得加上
           if (haveCreatePath == false) {
               File file = new File(fileUploadPath);
               file.mkdirs();
               haveCreatePath = true;
           }
           return fileUploadPath;
       }
   }

    public void setFileUploadPath(String fileUploadPath) {
        this.fileUploadPath = fileUploadPath;
    }

    public Boolean getKaptchaOpen() {
        return kaptchaOpen;
    }

    public void setKaptchaOpen(Boolean kaptchaOpen) {
        this.kaptchaOpen = kaptchaOpen;
    }

    public Boolean getSwaggerOpen() {
        return swaggerOpen;
    }

    public void setSwaggerOpen(Boolean swaggerOpen) {
        this.swaggerOpen = swaggerOpen;
    }

    public Boolean getSpringSessionOpen() {
        return springSessionOpen;
    }

    public void setSpringSessionOpen(Boolean springSessionOpen) {
        this.springSessionOpen = springSessionOpen;
    }

    public Integer getSessionInvalidateTime() {
        return sessionInvalidateTime;
    }

    public void setSessionInvalidateTime(Integer sessionInvalidateTime) {
        this.sessionInvalidateTime = sessionInvalidateTime;
    }

    public Integer getSessionValidationInterval() {
        return sessionValidationInterval;
    }

    public void setSessionValidationInterval(Integer sessionValidationInterval) {
        this.sessionValidationInterval = sessionValidationInterval;
    }
}
