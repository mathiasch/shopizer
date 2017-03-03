package com.salesmanager.web.tags;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.tags.RequestContextAwareTag;

import com.salesmanager.core.business.merchant.model.MerchantStore;
import com.salesmanager.web.constants.Constants;
import com.salesmanager.web.utils.FilePathUtils;
import com.salesmanager.web.utils.ImageFilePath;

public class StoreLogoUrlTag extends RequestContextAwareTag {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6319855234657139862L;
	private static final Logger LOGGER = LoggerFactory.getLogger(StoreLogoUrlTag.class);
	private static final String RESOURCES = "resources";
	private static final String IMG = "img";
	private static final String SHOPIZER_LOGO = "shopizer_small.png";
	
	@Autowired
	private FilePathUtils filePathUtils;


	@Autowired
	@Qualifier("img")
	private ImageFilePath imageUtils;

	public int doStartTagInternal() throws JspException {
		try {
			
			if (filePathUtils==null || imageUtils!=null) {
	            WebApplicationContext wac = getRequestContext().getWebApplicationContext();
	            AutowireCapableBeanFactory factory = wac.getAutowireCapableBeanFactory();
	            factory.autowireBean(this);
	        }

			HttpServletRequest request = (HttpServletRequest) pageContext
					.getRequest();
			
			MerchantStore merchantStore = (MerchantStore)request.getAttribute(Constants.MERCHANT_STORE);

			StringBuilder imagePath = new StringBuilder();
			
			String baseUrl = filePathUtils.buildRelativeStoreUri(request, merchantStore);
			imagePath.append(baseUrl);
			
			if(StringUtils.isBlank(merchantStore.getStoreLogo())){

				imagePath
					.append(RESOURCES).append("/")
					.append(IMG).append("/").append(SHOPIZER_LOGO);
			} else {
				
				imagePath
					.append(imageUtils.buildStoreLogoFilePath(merchantStore));
				
			}

			pageContext.getOut().print(imagePath.toString());


			
		} catch (Exception ex) {
			LOGGER.error("Error while getting content url", ex);
		}
		return SKIP_BODY;
	}

	public int doEndTag() {
		return EVAL_PAGE;
	}









	

}
