package com.xiaoxin.pan.swagger2;

import com.xiaoxin.pan.core.constants.XPanConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * swagger2配置属性实体
 */
@Data
@Component
@ConfigurationProperties(prefix = "swagger2")
public class Swagger2ConfigProperties {

    private boolean show = true;

    private String groupName = "x-pan";

    private String basePackage = XPanConstants.BASE_COMPONENT_SCAN_PATH;

    private String title = "x-pan-server";

    private String description = "x-pan-server";

    private String termsOfServiceUrl = "http://127.0.0.1:${server.port}";

    private String contactName = "xiaoxin";

    private String contactUrl = "https://blog.rubinchu.com";

    private String contactEmail = "2014402458@qq.com";

    private String version = "1.0";

}
