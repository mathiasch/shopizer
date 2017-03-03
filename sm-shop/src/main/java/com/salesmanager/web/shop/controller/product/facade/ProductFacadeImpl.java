package com.salesmanager.web.shop.controller.product.facade;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.salesmanager.core.business.catalog.category.service.CategoryService;
import com.salesmanager.core.business.catalog.product.model.Product;
import com.salesmanager.core.business.catalog.product.model.availability.ProductAvailability;
import com.salesmanager.core.business.catalog.product.model.price.ProductPrice;
import com.salesmanager.core.business.catalog.product.service.PricingService;
import com.salesmanager.core.business.catalog.product.service.ProductService;
import com.salesmanager.core.business.catalog.product.service.attribute.ProductOptionService;
import com.salesmanager.core.business.catalog.product.service.attribute.ProductOptionValueService;
import com.salesmanager.core.business.catalog.product.service.manufacturer.ManufacturerService;
import com.salesmanager.core.business.merchant.model.MerchantStore;
import com.salesmanager.core.business.reference.language.model.Language;
import com.salesmanager.core.business.reference.language.service.LanguageService;
import com.salesmanager.core.business.tax.service.TaxClassService;
import com.salesmanager.web.entity.catalog.product.PersistableProduct;
import com.salesmanager.web.entity.catalog.product.ProductPriceEntity;
import com.salesmanager.web.entity.catalog.product.ReadableProduct;
import com.salesmanager.web.populator.catalog.PersistableProductPopulator;
import com.salesmanager.web.populator.catalog.ReadableProductPopulator;
import com.salesmanager.web.utils.DateUtil;
import com.salesmanager.web.utils.ImageFilePath;

@Service("productFacade")
public class ProductFacadeImpl implements ProductFacade {
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private ManufacturerService manufacturerService;
	
	@Autowired
	private LanguageService languageService;
	
	@Autowired
	private ProductOptionService productOptionService;
	
	@Autowired
	private ProductOptionValueService productOptionValueService;
	
	@Autowired
	private TaxClassService taxClassService;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private PricingService pricingService;
	
	@Autowired
	@Qualifier("img")
	private ImageFilePath imageUtils;

	@Override
	public PersistableProduct saveProduct(MerchantStore store, PersistableProduct product, Language language)
			throws Exception {
		
		
		PersistableProductPopulator persistableProductPopulator = new PersistableProductPopulator();
		
		persistableProductPopulator.setCategoryService(categoryService);
		persistableProductPopulator.setManufacturerService(manufacturerService);
		persistableProductPopulator.setLanguageService(languageService);
		persistableProductPopulator.setProductOptionService(productOptionService);
		persistableProductPopulator.setProductOptionValueService(productOptionValueService);
		persistableProductPopulator.setTaxClassService(taxClassService);
		
		Product target = new Product();
		
		persistableProductPopulator.populate(product, target, store, language);
		
		productService.saveOrUpdate(target);
		
		product.setId(target.getId());
		
		return product;
		

	}

	@Override
	public ReadableProduct getProduct(MerchantStore store, Long id, Language language)
			throws Exception {

		Product product = productService.getById(id);
		
		if(product==null) {
			return null;
		}
		
		ReadableProduct readableProduct = new ReadableProduct();
		
		ReadableProductPopulator populator = new ReadableProductPopulator();
		
		populator.setPricingService(pricingService);
		populator.setimageUtils(imageUtils);
		populator.populate(product, readableProduct, store, language);
		
		return readableProduct;
	}

	@Override
	public ReadableProduct getProduct(MerchantStore store, String sku,
			Language language) throws Exception {
		
		Product product = productService.getByCode(sku, language);
		
		if(product==null) {
			return null;
		}
		
		ReadableProduct readableProduct = new ReadableProduct();
		
		ReadableProductPopulator populator = new ReadableProductPopulator();
		
		populator.setPricingService(pricingService);
		populator.setimageUtils(imageUtils);
		populator.populate(product, readableProduct, store, language);
		
		return readableProduct;
	}

	@Override
	public ReadableProduct updateProductPrice(ReadableProduct product,
			ProductPriceEntity price, Language language) throws Exception {
		
		
		Product persistable = productService.getById(product.getId());
		
		if(persistable==null) {
			throw new Exception("product is null for id " + product.getId());
		}
		
		java.util.Set<ProductAvailability> availabilities = persistable.getAvailabilities();
		for(ProductAvailability availability : availabilities) {
			ProductPrice productPrice = availability.defaultPrice();
			productPrice.setProductPriceAmount(price.getOriginalPrice());
			if(price.isDiscounted()) {
				productPrice.setProductPriceSpecialAmount(price.getDiscountedPrice());
				if(!StringUtils.isBlank(price.getDiscountStartDate())) {
					Date startDate = DateUtil.getDate(price.getDiscountStartDate());
					productPrice.setProductPriceSpecialStartDate(startDate);
				}
				if(!StringUtils.isBlank(price.getDiscountEndDate())) {
					Date endDate = DateUtil.getDate(price.getDiscountEndDate());
					productPrice.setProductPriceSpecialEndDate(endDate);
				}
			}
			
		}
		
		productService.saveOrUpdate(persistable);
		
		ReadableProduct readableProduct = new ReadableProduct();
		
		ReadableProductPopulator populator = new ReadableProductPopulator();
		
		populator.setPricingService(pricingService);
		populator.setimageUtils(imageUtils);
		populator.populate(persistable, readableProduct, persistable.getMerchantStore(), language);
		
		return readableProduct;
	}

	@Override
	public ReadableProduct updateProductQuantity(ReadableProduct product,
			int quantity, Language language) throws Exception {
		Product persistable = productService.getById(product.getId());
		
		if(persistable==null) {
			throw new Exception("product is null for id " + product.getId());
		}
		
		java.util.Set<ProductAvailability> availabilities = persistable.getAvailabilities();
		for(ProductAvailability availability : availabilities) {
			availability.setProductQuantity(quantity);
		}
		
		productService.saveOrUpdate(persistable);
		
		ReadableProduct readableProduct = new ReadableProduct();
		
		ReadableProductPopulator populator = new ReadableProductPopulator();
		
		populator.setPricingService(pricingService);
		populator.setimageUtils(imageUtils);
		populator.populate(persistable, readableProduct, persistable.getMerchantStore(), language);
		
		return readableProduct;
	}

}
