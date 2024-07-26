package com.xiaoxin.pan.server;

import com.xiaoxin.pan.core.constants.XPanConstants;
import com.xiaoxin.pan.core.response.R;
import io.swagger.annotations.Api;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@SpringBootApplication(scanBasePackages = XPanConstants.BASE_COMPONENT_SCAN_PATH)
@ServletComponentScan(basePackages = XPanConstants.BASE_COMPONENT_SCAN_PATH)
@RestController
@Api("x-pan-server")
@Validated
public class XPanServerLauncher {

    public static void main(String[] args) {
        SpringApplication.run(XPanServerLauncher.class);
    }

    @PostMapping("/hello")
    public R<String> hello(@Valid @RequestBody Test name){
        return R.success("hello " + name.toString());
    }

    public static class Test {
        @NotBlank
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Test{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

}
