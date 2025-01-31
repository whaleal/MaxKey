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
 

package org.maxkey.authz.oauth2.provider.userinfo.endpoint;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.maxkey.authn.SigninPrincipal;
import org.maxkey.authz.oauth2.common.OAuth2Constants;
import org.maxkey.authz.oauth2.common.exceptions.OAuth2Exception;
import org.maxkey.authz.oauth2.provider.ClientDetailsService;
import org.maxkey.authz.oauth2.provider.OAuth2Authentication;
import org.maxkey.authz.oauth2.provider.token.DefaultTokenServices;
import org.maxkey.constants.ContentType;
import org.maxkey.crypto.ReciprocalUtils;
import org.maxkey.crypto.jwt.encryption.service.JwtEncryptionAndDecryptionService;
import org.maxkey.crypto.jwt.encryption.service.impl.RecipientJwtEncryptionAndDecryptionServiceBuilder;
import org.maxkey.crypto.jwt.signer.service.JwtSigningAndValidationService;
import org.maxkey.crypto.jwt.signer.service.impl.SymmetricSigningAndValidationServiceBuilder;
import org.maxkey.entity.UserInfo;
import org.maxkey.entity.apps.oauth2.provider.ClientDetails;
import org.maxkey.persistence.service.AppsService;
import org.maxkey.persistence.service.UserInfoService;
import org.maxkey.util.JsonUtils;
import org.maxkey.util.StringGenerator;
import org.maxkey.web.HttpResponseAdapter;
import org.maxkey.web.WebConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.nimbusds.jwt.SignedJWT;

@Api(tags = "2-1-OAuth v2.0 API文档模块")
@Controller
public class OpenIdConnectUserInfoEndpoint {
	final static Logger _logger = LoggerFactory.getLogger(OpenIdConnectUserInfoEndpoint.class);	
	@Autowired
	@Qualifier("oauth20JdbcClientDetailsService")
	private ClientDetailsService clientDetailsService;
	
	@Autowired
	@Qualifier("oauth20TokenServices")
	private DefaultTokenServices oauth20tokenServices;
	
	
	@Autowired
	@Qualifier("userInfoService")
	private UserInfoService userInfoService;
	
	@Autowired
	@Qualifier("appsService")
	protected AppsService appsService;
	
	@Autowired
	@Qualifier("jwtSignerValidationService")
	private JwtSigningAndValidationService jwtSignerValidationService;
	
	@Autowired
	@Qualifier("jwtEncryptionService")
	private JwtEncryptionAndDecryptionService jwtEnDecryptionService; 
	
	
	
	private SymmetricSigningAndValidationServiceBuilder symmetricJwtSignerServiceBuilder
					=new SymmetricSigningAndValidationServiceBuilder();

	private RecipientJwtEncryptionAndDecryptionServiceBuilder recipientJwtEnDecryptionServiceBuilder
					=new RecipientJwtEncryptionAndDecryptionServiceBuilder();

	OAuthDefaultUserInfoAdapter defaultOAuthUserInfoAdapter=new OAuthDefaultUserInfoAdapter();
	
    @Autowired
    protected HttpResponseAdapter httpResponseAdapter;
		
	@ApiOperation(value = "OIDC 用户信息接口", notes = "传递Authorization参数access_token",httpMethod="GET")
	@RequestMapping(value=OAuth2Constants.ENDPOINT.ENDPOINT_OPENID_CONNECT_USERINFO)
	@ResponseBody
	public String connect10aUserInfo(
			@RequestHeader(value = "Authorization", required = true) String access_token,
			HttpServletRequest request, 
			HttpServletResponse response) {
		String principal="";
		if (!StringGenerator.uuidMatches(access_token)) {
			return JsonUtils.gson2Json(accessTokenFormatError(access_token));
		}
		
		OAuth2Authentication oAuth2Authentication =null;
		try{
			 oAuth2Authentication = oauth20tokenServices.loadAuthentication(access_token);
			 
			 principal=((SigninPrincipal)oAuth2Authentication.getPrincipal()).getUsername();
			 
			 Set<String >scopes=oAuth2Authentication.getOAuth2Request().getScope();
			 ClientDetails clientDetails = clientDetailsService.loadClientByClientId(oAuth2Authentication.getOAuth2Request().getClientId());
			 
			 UserInfo userInfo=queryUserInfo(principal);
			 String userJson="";
			 Builder jwtClaimsSetBuilder= new JWTClaimsSet.Builder();
			 
			 SigninPrincipal authentication = (SigninPrincipal)oAuth2Authentication.getUserAuthentication().getPrincipal();
			 
			 jwtClaimsSetBuilder.claim("sub", userInfo.getId());
			 jwtClaimsSetBuilder.claim(WebConstants.ONLINE_TICKET_NAME, authentication.getOnlineTicket().getTicketId());
			 
		 	if(scopes.contains("profile")){
		 		jwtClaimsSetBuilder.claim("name", userInfo.getUsername());
		 		jwtClaimsSetBuilder.claim("preferred_username", userInfo.getDisplayName());
		 		jwtClaimsSetBuilder.claim("given_name", userInfo.getGivenName());
		 		jwtClaimsSetBuilder.claim("family_name", userInfo.getFamilyName());
		 		jwtClaimsSetBuilder.claim("middle_name", userInfo.getMiddleName());
		 		jwtClaimsSetBuilder.claim("nickname", userInfo.getNickName());
		 		jwtClaimsSetBuilder.claim("profile", "profile");
		 		jwtClaimsSetBuilder.claim("picture", "picture");
		 		jwtClaimsSetBuilder.claim("website", userInfo.getWebSite());
				
				String gender;
				 switch(userInfo.getGender()){
				 	case UserInfo.GENDER.MALE  :
				 		gender="male";break;
				 	case UserInfo.GENDER.FEMALE  :
				 		gender="female";break;
				 	default:
				 		gender="unknown";
				 }
				jwtClaimsSetBuilder.claim("gender", gender);
				jwtClaimsSetBuilder.claim("zoneinfo", userInfo.getTimeZone());
				jwtClaimsSetBuilder.claim("locale", userInfo.getLocale());
				jwtClaimsSetBuilder.claim("updated_time", userInfo.getModifiedDate());
				jwtClaimsSetBuilder.claim("birthdate", userInfo.getBirthDate());
		 	}
		 	
		 	if(scopes.contains("email")){
		 		jwtClaimsSetBuilder.claim("email", userInfo.getWorkEmail());
		 		jwtClaimsSetBuilder.claim("email_verified", false);
		 	}
		 	
			if(scopes.contains("phone")){
				jwtClaimsSetBuilder.claim("phone_number", userInfo.getWorkPhoneNumber());
				jwtClaimsSetBuilder.claim("phone_number_verified", false);
			}
			
			if(scopes.contains("address")){
				HashMap<String, String> addressFields = new HashMap<String, String>();
				addressFields.put("country", userInfo.getWorkCountry());
				addressFields.put("region", userInfo.getWorkRegion());
				addressFields.put("locality", userInfo.getWorkLocality());
				addressFields.put("street_address", userInfo.getWorkStreetAddress());
				addressFields.put("formatted", userInfo.getWorkAddressFormatted());
				addressFields.put("postal_code", userInfo.getWorkPostalCode());
				
				jwtClaimsSetBuilder.claim("address", addressFields);
			}
			
			jwtClaimsSetBuilder
					.jwtID(UUID.randomUUID().toString())// set a random NONCE in the middle of it
					.audience(Arrays.asList(clientDetails.getClientId()))
					.issueTime(new Date())
					.expirationTime(new Date(new Date().getTime()+clientDetails.getAccessTokenValiditySeconds()*1000));
			
			JWTClaimsSet userInfoJWTClaims = jwtClaimsSetBuilder.build();
			JWT userInfoJWT=null;
			JWSAlgorithm signingAlg = jwtSignerValidationService.getDefaultSigningAlgorithm();
			if (clientDetails.getUserInfoEncryptedAlgorithm() != null 
			        && !clientDetails.getUserInfoEncryptedAlgorithm().equals("none")
					&& clientDetails.getUserInfoEncryptionMethod() != null 
					&& !clientDetails.getUserInfoEncryptionMethod().equals("none")
					&&clientDetails.getJwksUri()!=null&&clientDetails.getJwksUri().length()>4
					) {
			    //需要加密
			    response.setContentType(ContentType.APPLICATION_JWT_UTF8);
				JwtEncryptionAndDecryptionService recipientJwtEnDecryptionService =
						recipientJwtEnDecryptionServiceBuilder.serviceBuilder(clientDetails.getJwksUri());
				
				if (recipientJwtEnDecryptionService != null) {
					JWEAlgorithm jweAlgorithm=new JWEAlgorithm(clientDetails.getUserInfoEncryptedAlgorithm());
					EncryptionMethod encryptionMethod=new EncryptionMethod(clientDetails.getUserInfoEncryptionMethod());
					EncryptedJWT encryptedJWT = new EncryptedJWT(new JWEHeader(jweAlgorithm, encryptionMethod), userInfoJWTClaims);
					recipientJwtEnDecryptionService.encryptJwt(encryptedJWT);
					userJson=encryptedJWT.serialize();
				}else{
					_logger.error("Couldn't find encrypter for client: " + clientDetails.getClientId());
					HashMap<String,Object>authzException=new HashMap<String,Object>();
					authzException.put(OAuth2Exception.ERROR, "error");
					authzException.put(OAuth2Exception.DESCRIPTION,"Couldn't find encrypter for client: " + clientDetails.getClientId());
					return JsonUtils.gson2Json(authzException);
				}	
			}else if (clientDetails.getUserInfoSigningAlgorithm()!=null 
			        && !clientDetails.getUserInfoSigningAlgorithm().equals("none")) {
			    //需要签名
			    response.setContentType(ContentType.APPLICATION_JWT_UTF8);
				// signed ID token
				if (signingAlg.equals(JWSAlgorithm.HS256)
						|| signingAlg.equals(JWSAlgorithm.HS384)
						|| signingAlg.equals(JWSAlgorithm.HS512)) {
					// sign it with the client's secret
					String client_secret=ReciprocalUtils.decoder(clientDetails.getClientSecret());
					
					JwtSigningAndValidationService symmetricJwtSignerService =symmetricJwtSignerServiceBuilder.serviceBuilder(client_secret);
					if(symmetricJwtSignerService!=null){
						userInfoJWTClaims = new JWTClaimsSet.Builder(userInfoJWTClaims).claim("kid", "SYMMETRIC-KEY").build();
						userInfoJWT = new SignedJWT(new JWSHeader(signingAlg), userInfoJWTClaims);
						symmetricJwtSignerService.signJwt((SignedJWT) userInfoJWT);
					}else{
						_logger.error("Couldn't create symmetric validator for client " + clientDetails.getClientId() + " without a client secret");
					}
				} else {
					userInfoJWTClaims = new JWTClaimsSet.Builder(userInfoJWTClaims).claim("kid", jwtSignerValidationService.getDefaultSignerKeyId()).build();
					userInfoJWT = new SignedJWT(new JWSHeader(signingAlg), userInfoJWTClaims);
					// sign it with the server's key
					jwtSignerValidationService.signJwt((SignedJWT) userInfoJWT);
				}
				userJson=userInfoJWT.serialize();
			}else {
			    //不需要加密和签名
                response.setContentType(ContentType.APPLICATION_JSON_UTF8);
                // unsigned ID token
                //userInfoJWT = new PlainJWT(userInfoJWTClaims);
                userJson=JsonUtils.gson2Json(jwtClaimsSetBuilder.getClaims());
            }
			 
			return userJson;
			 
		}catch(OAuth2Exception e){
			HashMap<String,Object>authzException=new HashMap<String,Object>();
			authzException.put(OAuth2Exception.ERROR, e.getOAuth2ErrorCode());
			authzException.put(OAuth2Exception.DESCRIPTION,e.getMessage());
			return JsonUtils.object2Json(authzException);
		}
	}
	
	public HashMap<String,Object> accessTokenFormatError(String access_token){
		HashMap<String,Object>atfe=new HashMap<String,Object>();
		atfe.put(OAuth2Exception.ERROR, "token Format Invalid");
		atfe.put(OAuth2Exception.DESCRIPTION, "access Token Format Invalid , access_token : "+access_token);
		
		return atfe;
	}

	public  UserInfo queryUserInfo(String userId){
		_logger.debug("userId : "+userId);
		UserInfo userInfo = (UserInfo) userInfoService.loadByUsername(userId);
		return userInfo;
	}


	public void setOauth20tokenServices(DefaultTokenServices oauth20tokenServices) {
		this.oauth20tokenServices = oauth20tokenServices;
	}
	


	public void setUserInfoService(UserInfoService userInfoService) {
		this.userInfoService = userInfoService;
	}

//
//
//	public void setJwtSignerValidationService(
//			JwtSigningAndValidationService jwtSignerValidationService) {
//		this.jwtSignerValidationService = jwtSignerValidationService;
//	}
//
//	public void setJwtEnDecryptionService(
//			JwtEncryptionAndDecryptionService jwtEnDecryptionService) {
//		this.jwtEnDecryptionService = jwtEnDecryptionService;
//	}
}
