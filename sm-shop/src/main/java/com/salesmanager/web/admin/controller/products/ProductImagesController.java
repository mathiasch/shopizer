package com.salesmanager.web.admin.controller.products;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.salesmanager.core.business.catalog.product.model.Product;
import com.salesmanager.core.business.catalog.product.model.image.ProductImage;
import com.salesmanager.core.business.catalog.product.service.ProductService;
import com.salesmanager.core.business.catalog.product.service.image.ProductImageService;
import com.salesmanager.core.business.merchant.model.MerchantStore;
import com.salesmanager.core.utils.ajax.AjaxPageableResponse;
import com.salesmanager.core.utils.ajax.AjaxResponse;
import com.salesmanager.web.admin.controller.ControllerConstants;
import com.salesmanager.web.admin.entity.content.ProductImages;
import com.salesmanager.web.admin.entity.web.Menu;
import com.salesmanager.web.constants.Constants;
import com.salesmanager.web.utils.ImageFilePath;
import com.salesmanager.web.utils.LabelUtils;

@Controller
public class ProductImagesController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProductImagesController.class);
	
	

	@Autowired
	private ProductService productService;
	

	@Autowired
	private ProductImageService productImageService;
	
	@Autowired
	private LabelUtils messages;
	
	@Autowired
	@Qualifier("img")
	private ImageFilePath imageUtils;
	

	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value="/admin/products/images/list.html", method=RequestMethod.GET)
	public String displayProductImages(@RequestParam("id") long productId, Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		
		setMenu(model,request);
		MerchantStore store = (MerchantStore)request.getAttribute(Constants.ADMIN_STORE);
		
		Product product = productService.getById(productId);
		
		if(product==null) {
			return "redirect:/admin/products/products.html";
		}
		
		if(product.getMerchantStore().getId().intValue()!=store.getId().intValue()) {
			return "redirect:/admin/products/products.html";
		}
		
		model.addAttribute("product",product);
		return ControllerConstants.Tiles.Product.productImages;
		
	}
	
	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value="/admin/products/images/url/list.html", method=RequestMethod.GET)
	public String displayProductImagesUrl(@RequestParam("id") long productId, Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		
		setMenu(model,request);
		MerchantStore store = (MerchantStore)request.getAttribute(Constants.ADMIN_STORE);
		
		Product product = productService.getById(productId);
		
		if(product==null) {
			return "redirect:/admin/products/products.html";
		}
		
		if(product.getMerchantStore().getId().intValue()!=store.getId().intValue()) {
			return "redirect:/admin/products/products.html";
		}
		
        Map< String, String > mediaTypes = new HashMap<String, String>();  
        mediaTypes.put("0", "IMAGE");  
        mediaTypes.put("1", "VIDEO");   
		
		ProductImage productImage = new ProductImage();
		
		model.addAttribute("productImage", productImage);
		model.addAttribute("product",product);
		model.addAttribute("mediaTypes",mediaTypes);
		return ControllerConstants.Tiles.Product.productImagesUrl;
		
	}
	
	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value="/admin/products/images/page.html", method=RequestMethod.POST, produces="application/json;text/plain;charset=UTF-8")
	public @ResponseBody String pageProductImages(HttpServletRequest request, HttpServletResponse response) {

		String sProductId = request.getParameter("productId");
		
		
		AjaxResponse resp = new AjaxResponse();
		
		Long productId;
		Product product = null;
		
		try {
			productId = Long.parseLong(sProductId);
		} catch (Exception e) {
			resp.setStatus(AjaxPageableResponse.RESPONSE_STATUS_FAIURE);
			resp.setErrorString("Product id is not valid");
			String returnString = resp.toJSONString();
			return returnString;
		}

		
		try {
			
			
			product = productService.getById(productId);

			MerchantStore store = (MerchantStore)request.getAttribute(Constants.ADMIN_STORE);
			
			if(product.getMerchantStore().getId().intValue()!=store.getId().intValue()) {
				return "redirect:/admin/products/products.html";
			}

			Set<ProductImage> images = product.getImages();
			
			if(images!=null) {
				
				for(ProductImage image : images) {
					
						String imagePath = imageUtils.buildProductimageUtils(store, product, image.getProductImage());
						
						Map entry = new HashMap();
						//entry.put("picture", new StringBuilder().append(request.getContextPath()).append(imagePath).toString());
						entry.put("picture", imagePath);
						entry.put("name", image.getProductImage());
						entry.put("id",image.getId());
						entry.put("defaultImageCheckmark", image.isDefaultImage() ? "/resources/img/admin/checkmark_checked.png" : "/resources/img/admin/checkmark_unchecked.png");
						
						resp.addDataEntry(entry);
					
				}
			}

			resp.setStatus(AjaxPageableResponse.RESPONSE_STATUS_SUCCESS);
		
		} catch (Exception e) {
			LOGGER.error("Error while paging products", e);
			resp.setStatus(AjaxPageableResponse.RESPONSE_STATUS_FAIURE);
			resp.setErrorMessage(e);
		}
		
		String returnString = resp.toJSONString();
		return returnString;


	}
	
	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value="/admin/products/images/url/page.html", method=RequestMethod.POST, produces="application/json")
	public @ResponseBody String pageProductImagesUrl(HttpServletRequest request, HttpServletResponse response) {

		String sProductId = request.getParameter("productId");
		
		
		AjaxResponse resp = new AjaxResponse();
		
		Long productId;
		Product product = null;
		
		try {
			productId = Long.parseLong(sProductId);
		} catch (Exception e) {
			resp.setStatus(AjaxPageableResponse.RESPONSE_STATUS_FAIURE);
			resp.setErrorString("Product id is not valid");
			String returnString = resp.toJSONString();
			return returnString;
		}

		
		try {
			
			
			product = productService.getById(productId);

			MerchantStore store = (MerchantStore)request.getAttribute(Constants.ADMIN_STORE);

			if(product.getMerchantStore().getId().intValue()!=store.getId().intValue()) {
				return "redirect:/admin/products/products.html";
			}
			
			Set<ProductImage> images = product.getImages();
			
			if(images!=null) {
				
				for(ProductImage image : images) {
					
					if(!StringUtils.isBlank(image.getProductImageUrl())) {

						Map entry = new HashMap();
						entry.put("image", image.getProductImageUrl());
						entry.put("url", image.getProductImageUrl());
						entry.put("default", image.isDefaultImage());
						entry.put("id",image.getId());
						
						resp.addDataEntry(entry);
					
					}
				}

			}



			resp.setStatus(AjaxPageableResponse.RESPONSE_STATUS_SUCCESS);
		
		} catch (Exception e) {
			LOGGER.error("Error while paging products", e);
			resp.setStatus(AjaxPageableResponse.RESPONSE_STATUS_FAIURE);
			resp.setErrorMessage(e);
		}
		
		String returnString = resp.toJSONString();
		return returnString;


	}


	
	
	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value="/admin/products/images/save.html", method=RequestMethod.POST)
	public String saveProductImages(@ModelAttribute(value="productImages") @Valid final ProductImages productImages, final BindingResult bindingResult,final Model model, final HttpServletRequest request,Locale locale) throws Exception{
	    
	    
		this.setMenu(model, request);

		
		MerchantStore store = (MerchantStore)request.getAttribute(Constants.ADMIN_STORE);

		Product product = productService.getById(productImages.getProductId());
		model.addAttribute("product",product);
		if(product==null) {
			FieldError error = new FieldError("productImages","image",messages.getMessage("message.error", locale));
			bindingResult.addError(error);
			return ControllerConstants.Tiles.Product.productImages;
		}
		
		if(product.getMerchantStore().getId().intValue()!=store.getId().intValue()) {
			FieldError error = new FieldError("productImages","image",messages.getMessage("message.error", locale));
			bindingResult.addError(error);
		}
		
		if (bindingResult.hasErrors()) {
	        LOGGER.info( "Found {} Validation errors", bindingResult.getErrorCount());
	       return ControllerConstants.Tiles.Product.productImages;
	       
        }
		
	    final List<ProductImage> contentImagesList=new ArrayList<ProductImage>();
        if(CollectionUtils.isNotEmpty( productImages.getFile() )){
            LOGGER.info("Saving {} content images for merchant {}",productImages.getFile().size(),store.getId());
            for(final MultipartFile multipartFile:productImages.getFile()){
                if(!multipartFile.isEmpty()){
                	ProductImage productImage = new ProductImage();

                	productImage.setImage(multipartFile.getInputStream());
                    productImage.setProductImage(multipartFile.getOriginalFilename() );
                    productImage.setProduct(product);
                    productImage.setDefaultImage(false);//default image is uploaded in the product details
                    
                    contentImagesList.add( productImage);
                }
            }
            
            if(CollectionUtils.isNotEmpty( contentImagesList )){
            	productImageService.addProductImages(product, contentImagesList);
            }
            
        }
		
        
        //reload
        product = productService.getById(productImages.getProductId());
        model.addAttribute("product",product);
        model.addAttribute("success","success");
        
        return ControllerConstants.Tiles.Product.productImages;
	}
	


	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value="/admin/products/images/url/save.html", method=RequestMethod.POST)
	public String saveProductImagesUrl(@ModelAttribute(value="productImage") @Valid final ProductImage productImage, final BindingResult bindingResult,final Model model, final HttpServletRequest request,Locale locale) throws Exception{
	    
	    
		this.setMenu(model, request);

		MerchantStore store = (MerchantStore)request.getAttribute(Constants.ADMIN_STORE);
		
        Map< String, String > mediaTypes = new HashMap<String, String>();  
        mediaTypes.put("0", "IMAGE");  
        mediaTypes.put("1", "VIDEO");   

		model.addAttribute("productImage", productImage);
		model.addAttribute("mediaTypes",mediaTypes);

		Product product = productService.getById(productImage.getId());
		model.addAttribute("product",product);
		if(product==null) {
			FieldError error = new FieldError("productImages","image",messages.getMessage("message.error", locale));
			bindingResult.addError(error);
			return ControllerConstants.Tiles.Product.productImagesUrl;
		}
		
		if(product.getMerchantStore().getId().intValue()!=store.getId().intValue()) {
			FieldError error = new FieldError("productImages","image",messages.getMessage("message.error", locale));
			bindingResult.addError(error);
		}
		
		model.addAttribute("product",product);
		
		if (bindingResult.hasErrors()) {
	        LOGGER.info( "Found {} Validation errors", bindingResult.getErrorCount());
	       return ControllerConstants.Tiles.Product.productImagesUrl;
        }
		
		productImage.setProduct(product);
		productImage.setId(null);
		
		productImageService.saveOrUpdate(productImage);
        model.addAttribute("product",product);
        model.addAttribute("success","success");
        
        return ControllerConstants.Tiles.Product.productImagesUrl;
	}

	
	

		
	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value="/admin/products/images/remove.html", method=RequestMethod.POST, produces="application/json")
	public @ResponseBody String deleteImage(HttpServletRequest request, HttpServletResponse response, Locale locale) {
		String sImageId = request.getParameter("id");

		
		MerchantStore store = (MerchantStore)request.getAttribute(Constants.ADMIN_STORE);
		AjaxResponse resp = new AjaxResponse();

		
		try {
			
			
		
				
			Long imageId = Long.parseLong(sImageId);

			
			ProductImage productImage = productImageService.getById(imageId);
			if(productImage==null) {
				resp.setStatusMessage(messages.getMessage("message.unauthorized", locale));
				resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);			
				return resp.toJSONString();
			}
			
			if(productImage.getProduct().getMerchantStore().getId().intValue()!=store.getId().intValue()) {
				resp.setStatusMessage(messages.getMessage("message.unauthorized", locale));
				resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);			
				return resp.toJSONString();
			}
			
			productImageService.removeProductImage(productImage);

			resp.setStatus(AjaxResponse.RESPONSE_OPERATION_COMPLETED);

		
		
		} catch (Exception e) {
			LOGGER.error("Error while deleting product price", e);
			resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);
			resp.setErrorMessage(e);
		}
		
		String returnString = resp.toJSONString();
		
		return returnString;
	}
	
	
	@PreAuthorize("hasRole('PRODUCTS')")
	@RequestMapping(value="/admin/products/images/defaultImage.html", method=RequestMethod.POST, produces="application/json")
	public @ResponseBody String setDefaultImage(final HttpServletRequest request, 
												final HttpServletResponse response, 
												final Locale locale) {
		final String sImageId = request.getParameter("id");
		final MerchantStore store = (MerchantStore)request.getAttribute(Constants.ADMIN_STORE);
		final AjaxResponse resp = new AjaxResponse();

		try {
			final Long imageId = Long.parseLong(sImageId);
			final ProductImage productImage = productImageService.getById(imageId);
			
			if (productImage == null) {
				resp.setStatusMessage(messages.getMessage("message.unauthorized", locale));
				resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);			
				return resp.toJSONString();
			}
			
			if (productImage.getProduct().getMerchantStore().getId().intValue() != store.getId().intValue()) {
				resp.setStatusMessage(messages.getMessage("message.unauthorized", locale));
				resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);			
				return resp.toJSONString();
			}
			
			productImage.setDefaultImage(true);
			productImageService.saveOrUpdate(productImage);
			
			final Set<ProductImage> images = productService.getById(productImage.getProduct().getId()).getImages();
			for (final ProductImage image : images) {
				if (image.getId() != productImage.getId()) {
					image.setDefaultImage(false);
					productImageService.saveOrUpdate(image);		
				}
			}
			
			resp.setStatus(AjaxResponse.RESPONSE_OPERATION_COMPLETED);
		} catch (final Exception e) {
			LOGGER.error("Error while set default image", e);
			resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);
			resp.setErrorMessage(e);
		}
		
		final String returnString = resp.toJSONString();
		return returnString;
	}

	
	private void setMenu(Model model, HttpServletRequest request) throws Exception {
		
		//display menu
		Map<String,String> activeMenus = new HashMap<String,String>();
		activeMenus.put("catalogue", "catalogue");
		activeMenus.put("catalogue-products", "catalogue-products");
		
		@SuppressWarnings("unchecked")
		Map<String, Menu> menus = (Map<String, Menu>)request.getAttribute("MENUMAP");
		
		Menu currentMenu = (Menu)menus.get("catalogue");
		model.addAttribute("currentMenu",currentMenu);
		model.addAttribute("activeMenus",activeMenus);
		//
		
	}

}
