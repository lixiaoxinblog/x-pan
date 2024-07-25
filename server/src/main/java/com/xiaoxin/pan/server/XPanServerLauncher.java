package com.xiaoxin.pan.server;

import com.xiaoxin.pan.core.constants.XPanConstants;
import com.xiaoxin.pan.core.response.R;
import io.swagger.annotations.Api;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = XPanConstants.BASE_COMPONENT_SCAN_PATH)
@ServletComponentScan(basePackages = XPanConstants.BASE_COMPONENT_SCAN_PATH)
@RestController
@Api("x-pan-server")
public class XPanServerLauncher {

    public static void main(String[] args) {
        SpringApplication.run(XPanServerLauncher.class);
    }

    @RequestMapping("/hello")
    public R<String> hello(String name){
        return R.success("hello " + name);
    }

}
