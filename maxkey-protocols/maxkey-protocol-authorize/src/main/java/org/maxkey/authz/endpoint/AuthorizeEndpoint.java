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
 

/**
 * 
 */
package org.maxkey.authz.endpoint;

import javax.servlet.http.HttpServletRequest;


import org.maxkey.constants.ConstantsProtocols;
import org.maxkey.entity.apps.Apps;
import org.maxkey.persistence.service.AppsCasDetailsService;
import org.maxkey.web.WebConstants;
import org.maxkey.web.WebContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author Crystal.Sea
 *
 */
@Api(tags = "1-2认证总地址文档模块")
@Controller
public class AuthorizeEndpoint extends AuthorizeBaseEndpoint{
	@Autowired
	AppsCasDetailsService casDetailsService;
	
	//all single sign on url
	@ApiOperation(value = "认证总地址接口", notes = "参数应用ID，分发到不同应用的认证地址",httpMethod="GET")
	@RequestMapping("/authz/{id}")
	public ModelAndView authorize(
			HttpServletRequest request,
			@PathVariable("id") String id){
		ModelAndView modelAndView=null;
		Apps  application=getApp(id);
		id = application.getId();
		WebContext.setAttribute(WebConstants.SINGLE_SIGN_ON_APP_ID, application.getId());
		
		if(application.getProtocol().equalsIgnoreCase(ConstantsProtocols.EXTEND_API)){
			modelAndView=WebContext.forward("/authz/api/"+id);
		}else if (application.getProtocol().equalsIgnoreCase(ConstantsProtocols.FORMBASED)){
			 modelAndView=WebContext.forward("/authz/formbased/"+id);
		}else if (application.getProtocol().equalsIgnoreCase(ConstantsProtocols.OAUTH20)){
			 modelAndView=WebContext.forward("/authz/oauth/v20/"+application.getId());
		}else if (application.getProtocol().equalsIgnoreCase(ConstantsProtocols.OPEN_ID_CONNECT)){
			// modelAndView=new ModelAndView("openid connect");
		}else if (application.getProtocol().equalsIgnoreCase(ConstantsProtocols.SAML20)){
			 modelAndView=WebContext.forward("/authz/saml20/idpinit/"+application.getId());
		}else if (application.getProtocol().equalsIgnoreCase(ConstantsProtocols.TOKENBASED)){
			modelAndView=WebContext.forward("/authz/tokenbased/"+id);
		}else if (application.getProtocol().equalsIgnoreCase(ConstantsProtocols.CAS)){
			modelAndView=WebContext.forward("/authz/cas/"+id);
		}else if (application.getProtocol().equalsIgnoreCase(ConstantsProtocols.JWT)){
            modelAndView=WebContext.forward("/authz/jwt/"+id);
        }else if (application.getProtocol().equalsIgnoreCase(ConstantsProtocols.BASIC)){
			modelAndView=WebContext.redirect(application.getLoginUrl());
		}
		
		_logger.debug(modelAndView.getViewName());
		
		return modelAndView;
	}
	
}
