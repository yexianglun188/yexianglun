package com.campus.market.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.common.BusinessException;
import com.campus.market.common.PageResult;
import com.campus.market.common.ResultCode;
import com.campus.market.dto.ProductFormDTO;
import com.campus.market.dto.ProductQuery;
import com.campus.market.entity.Favorite;
import com.campus.market.entity.Product;
import com.campus.market.mapper.FavoriteMapper;
import com.campus.market.mapper.ProductMapper;
import com.campus.market.service.ProductService;
import com.campus.market.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final FavoriteMapper favoriteMapper;

    @Override
    public PageResult<Product> pageOnSale(ProductQuery query) {
        // 前台列表只查在售商品
        query.setStatus(Product.STATUS_ON_SALE);
        return doPage(query);
    }

    @Override
    public Product getDetail(Long id) {
        Product product = productMapper.selectDetailById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "商品不存在或已被删除");
        }

        // 浏览量 +1（使用 SQL 自增，避免并发覆盖）
        productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, id)
                .setSql("view_count = view_count + 1"));

        product.setImageList(StrUtil.isBlank(product.getImages())
                ? Collections.emptyList()
                : StrUtil.split(product.getImages(), ','));

        // 标记当前登录用户是否已收藏
        Long userId = UserContext.getUserId();
        if (userId != null) {
            Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                    .eq(Favorite::getUserId, userId)
                    .eq(Favorite::getProductId, id));
            product.setFavorited(count != null && count > 0);
        } else {
            product.setFavorited(false);
        }
        return product;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publish(ProductFormDTO dto) {
        Product product = buildProduct(dto);
        product.setUserId(UserContext.requireUserId());
        // 演示环境直接上架；若需审核流程，改为 Product.STATUS_PENDING
        product.setStatus(Product.STATUS_ON_SALE);
        product.setViewCount(0);
        product.setFavoriteCount(0);
        productMapper.insert(product);
        return product.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ProductFormDTO dto) {
        Product exists = requireOwnedProduct(dto.getId());
        Product product = buildProduct(dto);
        product.setId(exists.getId());
        productMapper.updateById(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offline(Long id) {
        Product product = requireOwnedProduct(id);
        changeStatus(product, Product.STATUS_OFFLINE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void online(Long id) {
        Product product = requireOwnedProduct(id);
        changeStatus(product, Product.STATUS_ON_SALE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        Product product = requireOwnedProduct(id);
        if (product.getStatus() == Product.STATUS_SOLD) {
            throw new BusinessException("已售出的商品不能删除");
        }
        productMapper.deleteById(id);
    }

    @Override
    public PageResult<Product> myProducts(Integer pageNum, Integer pageSize, Integer status) {
        ProductQuery query = new ProductQuery();
        query.setSellerId(UserContext.requireUserId());
        query.setStatus(status);
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);
        query.setSort("latest");
        return doPage(query);
    }

    @Override
    public PageResult<Product> adminPage(ProductQuery query) {
        return doPage(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminOffline(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "商品不存在");
        }
        changeStatus(product, Product.STATUS_OFFLINE);
    }

    // ------------------------------------------------------------

    private PageResult<Product> doPage(ProductQuery query) {
        Page<Product> page = new Page<>(
                query.getPageNum() == null ? 1 : query.getPageNum(),
                query.getPageSize() == null ? 12 : query.getPageSize());
        IPage<Product> result = productMapper.selectProductPage(page, query);
        List<Product> records = result.getRecords();
        records.forEach(p -> p.setImageList(StrUtil.isBlank(p.getImages())
                ? Collections.emptyList()
                : StrUtil.split(p.getImages(), ',')));
        return PageResult.of(result.getTotal(), records);
    }

    private Product buildProduct(ProductFormDTO dto) {
        Product product = new Product();
        BeanUtil.copyProperties(dto, product, "images");
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            product.setImages(StrUtil.join(",", dto.getImages()));
            if (StrUtil.isBlank(dto.getCoverImage())) {
                product.setCoverImage(dto.getImages().get(0));
            }
        }
        if (StrUtil.isBlank(product.getItemCondition())) {
            product.setItemCondition("几乎全新");
        }
        return product;
    }

    /** 校验商品存在且属于当前用户 */
    private Product requireOwnedProduct(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "商品不存在");
        }
        if (!product.getUserId().equals(UserContext.requireUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "只能操作自己发布的商品");
        }
        return product;
    }

    private void changeStatus(Product product, int status) {
        Product update = new Product();
        update.setId(product.getId());
        update.setStatus(status);
        productMapper.updateById(update);
    }
}
