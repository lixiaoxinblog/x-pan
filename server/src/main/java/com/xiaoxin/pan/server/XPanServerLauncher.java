package com.xiaoxin.pan.server;

import com.xiaoxin.pan.core.constants.XPanConstants;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication(scanBasePackages = XPanConstants.BASE_COMPONENT_SCAN_PATH)
@ServletComponentScan(basePackages = XPanConstants.BASE_COMPONENT_SCAN_PATH)
@EnableAsync
public class XPanServerLauncher {

    public static void main(String[] args) {
        SpringApplication.run(XPanServerLauncher.class);
    }

}
