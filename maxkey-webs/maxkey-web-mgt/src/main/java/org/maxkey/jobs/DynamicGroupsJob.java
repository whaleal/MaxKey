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
 

package org.maxkey.jobs;

import java.io.Serializable;
import java.util.List;

import org.maxkey.entity.Groups;
import org.maxkey.persistence.service.GroupsService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DynamicGroupsJob  implements Job , Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 8831626240807856084L;

    final static Logger _logger = LoggerFactory.getLogger(DynamicGroupsJob.class);
    
    private static  GroupsService groupsService = null;
    
    public static class JOBSTATUS{
        public static int STOP = 0;
        public static int RUNNING = 1;
        public static int FINISHED = 2;
    }
    
    private static int jobStatus = JOBSTATUS.STOP;

    @Override
    public void execute(JobExecutionContext context){
        if(jobStatus == JOBSTATUS.RUNNING) {
            _logger.info("DynamicGroupsJob is in running . " );
            return;
        }
        
        _logger.debug("DynamicGroupsJob is running ... " );
        jobStatus = JOBSTATUS.RUNNING;
        try {
            if(groupsService == null) {
                groupsService = (GroupsService) context.getMergedJobDataMap().get("groupsService");
            }

            List<Groups>  groupsList = groupsService.queryDynamicGroups(null);
            for(Groups group : groupsList) {
                _logger.debug("group " + group);
                groupsService.refreshDynamicGroups(group);
            }
            Thread.sleep(10 *1000);
            _logger.debug("DynamicGroupsJob is success  " );
        }catch(Exception e) {
            _logger.error("Exception " ,e);
            jobStatus = JOBSTATUS.STOP;
        }
        jobStatus = JOBSTATUS.FINISHED;
        _logger.debug("DynamicGroupsJob is finished . " );
    }


}
