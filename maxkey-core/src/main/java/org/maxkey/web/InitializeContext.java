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
 

package org.maxkey.web;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.joda.time.DateTime;
import org.maxkey.cache.CacheFactory;
import org.maxkey.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * InitApplicationContext .
 * @author Crystal.Sea
 *
 */
public class InitializeContext extends HttpServlet {
    private static final Logger _logger = LoggerFactory.getLogger(InitializeContext.class);
    private static final long serialVersionUID = -797399138268601444L;
    ApplicationContext applicationContext;
    

    @Override
    public String getServletInfo() {
        return super.getServletInfo();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        _logger.info("SecurityContextHolder StrategyName " + SessionSecurityContextHolderStrategy.class.getCanonicalName());
        SecurityContextHolder.setStrategyName(SessionSecurityContextHolderStrategy.class.getCanonicalName());
        
        WebContext.applicationContext = applicationContext;
        
        org.apache.mybatis.jpa.util.WebContext.applicationContext = applicationContext;
        
        // List Environment Variables
        listEnvVars();

        listProperties();

        // List DatabaseMetaData Variables
        listDataBaseVariables();

        // load caches
        loadCaches();

        // Show License
        showLicense();
    }

    /**
    * InitApplicationContext.
    */
    public InitializeContext() {
        this.applicationContext = 
                WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
    }

    public InitializeContext(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * loadCaches.
     */
    public void loadCaches() {
        _logger.info("-----------------------------------------------------------");
        _logger.info("Load Caches ");

        try {
            if (applicationContext.containsBean("cacheFactory")) {
                CacheFactory cacheFactory = 
                        applicationContext.getBean("cacheFactory", CacheFactory.class);
                cacheFactory.start();
            }
        } catch (BeansException e) {
            e.printStackTrace();
        }
        _logger.info("-----------------------------------------------------------");

    }

    /**
     * listDataBaseVariables.
     */
    public void listDataBaseVariables() {
        if (applicationContext.containsBean("dataSource")) {
            try {
                _logger.debug("-----------------------------------------------------------");
                _logger.debug("List DatabaseMetaData Variables ");
                Connection connection = 
                        ((javax.sql.DataSource) applicationContext.getBean("dataSource"))
                        .getConnection();

                java.sql.DatabaseMetaData databaseMetaData = connection.getMetaData();
                _logger.debug("DatabaseProductName   :   " 
                        + databaseMetaData.getDatabaseProductName());
                _logger.debug("DatabaseProductVersion:   " 
                        + databaseMetaData.getDatabaseProductVersion());
                _logger.trace("DatabaseMajorVersion  :   " 
                        + databaseMetaData.getDatabaseMajorVersion());
                _logger.trace("DatabaseMinorVersion  :   " 
                        + databaseMetaData.getDatabaseMinorVersion());
                _logger.trace("supportsTransactions  :   " 
                        + databaseMetaData.supportsTransactions());
                _logger.trace("DefaultTransaction    :   " 
                        + databaseMetaData.getDefaultTransactionIsolation());
                _logger.trace("MaxConnections        :   " 
                        + databaseMetaData.getMaxConnections());
                _logger.trace("");
                _logger.trace("JDBCMajorVersion      :   " 
                        + databaseMetaData.getJDBCMajorVersion());
                _logger.trace("JDBCMinorVersion      :   " 
                        + databaseMetaData.getJDBCMinorVersion());
                _logger.trace("DriverName            :   " 
                        + databaseMetaData.getDriverName());
                _logger.trace("DriverVersion         :   " 
                        + databaseMetaData.getDriverVersion());
                _logger.debug("");
                _logger.debug("DBMS  URL             :   " 
                        + databaseMetaData.getURL());
                _logger.debug("UserName              :   " 
                        + databaseMetaData.getUserName());
                _logger.debug("-----------------------------------------------------------");
            } catch (SQLException e) {
                e.printStackTrace();
                _logger.error("DatabaseMetaData Variables Error .",e);
            }
        }
    }

    /**
     * propertySourcesPlaceholderConfigurer.
     */
    public void listProperties() {
        if (applicationContext.containsBean("propertySourcesPlaceholderConfigurer")) {
            _logger.trace("-----------------------------------------------------------");
            _logger.trace("List Properties Variables ");
            PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = 
                    ((PropertySourcesPlaceholderConfigurer) applicationContext
                    .getBean("propertySourcesPlaceholderConfigurer"));
            
            WebContext.properties =  (StandardEnvironment) propertySourcesPlaceholderConfigurer
                    .getAppliedPropertySources()
                    .get(PropertySourcesPlaceholderConfigurer.ENVIRONMENT_PROPERTIES_PROPERTY_SOURCE_NAME)
                    .getSource();
  
            Iterator<PropertySource<?>> it =WebContext.properties.getPropertySources().iterator();
            while(it.hasNext()) {
            	 _logger.debug("propertySource " + it.next());
            }
            _logger.trace("-----------------------------------------------------------");
        }
    }

    /**
     * listEnvVars.
     */
    public void listEnvVars() {
        _logger.debug("-----------------------------------------------------------");
        _logger.debug("List Environment Variables ");
        Map<String, String> map = System.getenv();
        SortedSet<String> keyValueSet = new TreeSet<String>();
        for (Iterator<String> itr = map.keySet().iterator(); itr.hasNext();) {
            String key = itr.next();
            keyValueSet.add(key);
        }
        // out
        for (Iterator<String> it = keyValueSet.iterator(); it.hasNext();) {
            String key = (String) it.next();
            _logger.trace(key + "   =   " + map.get(key));
        }
        _logger.debug("APP_HOME" + "   =   " + PathUtils.getInstance().getAppPath());
        _logger.debug("-----------------------------------------------------------");
    }

    /**
     * showLicense.
     */
    public void showLicense() {
        _logger.info("-----------------------------------------------------------");
        _logger.info("+                                MaxKey ");
        _logger.info("+                      Single   Sign   On ( SSO ) ");
        _logger.info("+                           Version "
                    + WebContext.properties.getProperty("application.formatted-version"));
        _logger.info("+");
        _logger.info("+                  "+  ((char)0xA9) + "Copyright 2018-"
        			+ (new DateTime().getYear())
        			+ " https://www.maxkey.top/");
        _logger.info("+                 Licensed under the Apache License, Version 2.0 ");
        _logger.info("-----------------------------------------------------------");
    }

}
