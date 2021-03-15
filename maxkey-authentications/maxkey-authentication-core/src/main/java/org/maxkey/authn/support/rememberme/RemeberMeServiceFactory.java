/*
 * Copyright [2021] [MaxKey of copyright http://www.maxkey.top]
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
 

package org.maxkey.authn.support.rememberme;

import org.maxkey.constants.ConstantsPersistence;
import org.maxkey.persistence.redis.RedisConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class RemeberMeServiceFactory {
	private static final  Logger _logger = 
            LoggerFactory.getLogger(RemeberMeServiceFactory.class);
	
	 public AbstractRemeberMeService getService(
			 	int persistence,
			 	JdbcTemplate jdbcTemplate,
	            RedisConnectionFactory redisConnFactory){
		 
		 AbstractRemeberMeService remeberMeService = null;
	        if (persistence == ConstantsPersistence.INMEMORY) {
	            remeberMeService = new InMemoryRemeberMeService();
	            _logger.debug("InMemoryRemeberMeService");
	        } else if (persistence == ConstantsPersistence.JDBC) {
	            //remeberMeService = new JdbcRemeberMeService(jdbcTemplate);
	            _logger.debug("JdbcRemeberMeService not support "); 
	        } else if (persistence == ConstantsPersistence.REDIS) {
	            remeberMeService = new RedisRemeberMeService(redisConnFactory);
	            _logger.debug("RedisRemeberMeService");
	        }
	        return remeberMeService;
	}
}
