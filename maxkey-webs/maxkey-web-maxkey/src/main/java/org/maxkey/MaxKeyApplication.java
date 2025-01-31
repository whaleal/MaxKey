/*
 * Copyright [2020] [MaxKey of copyright http://www.maxkey.top]
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 

package org.maxkey;

import java.util.Date;

import javax.servlet.ServletException;
import org.apache.ibatis.io.VFS;
import org.apache.mybatis.jpa.SpringBootVFS;
import org.maxkey.configuration.ApplicationConfig;
import org.maxkey.web.InitializeContext;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("org.maxkey.persistence.mapper,")
public class MaxKeyApplication extends SpringBootServletInitializer {
    private static final Logger _logger = LoggerFactory.getLogger(MaxKeyApplication.class);

    /**
     * @param args start parameter 
     */
    public static void main(String[] args) {
        _logger.info("Start MaxKeyApplication ...");
        
        VFS.addImplClass(SpringBootVFS.class);
        ConfigurableApplicationContext applicationContext = 
                SpringApplication.run(MaxKeyApplication.class, args);
        InitializeContext initWebContext = new InitializeContext(applicationContext);
        try {
            initWebContext.init(null);
        } catch (ServletException e) {
            _logger.error("ServletException", e);
        }
        _logger.info("MaxKey at " + new Date(applicationContext.getStartupDate()));
        _logger.info("MaxKey Server Port "
                +   applicationContext.getBean(ApplicationConfig.class).getPort());
        _logger.info("MaxKey started.");
    }

    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(MaxKeyApplication.class);
    }

}
