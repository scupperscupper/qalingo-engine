/**
 * Most of the code in the Qalingo project is copyrighted Hoteia and licensed
 * under the Apache License Version 2.0 (release version 0.8.0)
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *                   Copyright (c) Hoteia, 2012-2014
 * http://www.hoteia.com - http://twitter.com/hoteia - contact@hoteia.com
 *
 */
package org.hoteia.qalingo.core.service.pojo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dozer.Mapper;
import org.hoteia.qalingo.core.domain.Cart;
import org.hoteia.qalingo.core.domain.CartItem;
import org.hoteia.qalingo.core.domain.CatalogCategoryVirtual;
import org.hoteia.qalingo.core.domain.DeliveryMethod;
import org.hoteia.qalingo.core.domain.MarketArea;
import org.hoteia.qalingo.core.domain.ProductMarketing;
import org.hoteia.qalingo.core.domain.ProductSku;
import org.hoteia.qalingo.core.pojo.cart.CartPojo;
import org.hoteia.qalingo.core.pojo.deliverymethod.DeliveryMethodPojo;
import org.hoteia.qalingo.core.pojo.util.mapper.PojoUtil;
import org.hoteia.qalingo.core.service.CatalogCategoryService;
import org.hoteia.qalingo.core.service.ProductService;
import org.hoteia.qalingo.core.service.pojo.CheckoutPojoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("checkoutPojoService")
@Transactional(readOnly = true)
public class CheckoutPojoFactory {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected ProductService productService;
    
    @Autowired 
    protected CatalogCategoryService catalogCategoryService;
    
    @Autowired 
    private Mapper dozerBeanMapper;
    
    public CartPojo handleCartMapping(final Cart cart, final String catalogVirtualCode, final String catalogMasterCode) {
        if(cart != null){
            Set<CartItem> cartItems = cart.getCartItems();
            for (Iterator<CartItem> iterator = cartItems.iterator(); iterator.hasNext();) {
                CartItem cartItem = (CartItem) iterator.next();
                if(cartItem.getProductSku() == null){
                    final ProductSku productSku = productService.getProductSkuByCode(cartItem.getProductSkuCode());
                    cartItem.setProductSku(productSku);
                }
                if(cartItem.getProductMarketing() == null){
                    final ProductMarketing productMarketing = productService.getProductMarketingByCode(cartItem.getProductMarketingCode());
                    cartItem.setProductMarketing(productMarketing);
                }
                if(cartItem.getCatalogCategory() == null){
                    final CatalogCategoryVirtual catalogCategory = catalogCategoryService.getVirtualCatalogCategoryByCode(cartItem.getCatalogCategoryCode(), catalogVirtualCode, catalogMasterCode);
                    cartItem.setCatalogCategory(catalogCategory);
                }
            }
        }
        return cart == null ? null : dozerBeanMapper.map(cart, CartPojo.class);
    }
    
    public List<DeliveryMethodPojo> getAvailableDeliveryMethodsByMarketArea(final MarketArea marketArea) {
        if(marketArea != null 
                && marketArea.getDeliveryMethods() != null){
            return getAvailableDeliveryMethods(new ArrayList<DeliveryMethod>(marketArea.getDeliveryMethods()));
        }
        return null;
    }
    
    public List<DeliveryMethodPojo> getAvailableDeliveryMethods(final List<DeliveryMethod> deliveryMethods) {
        if(deliveryMethods != null){
            logger.debug("Found {} deliveryMethods", deliveryMethods.size());
            return PojoUtil.mapAll(dozerBeanMapper, deliveryMethods, DeliveryMethodPojo.class);
        }
        return null;
    }
    
}