package com.ledger.db.impl.factory;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ledger.common.result.Result;
import com.ledger.db.entity.FactoryQuotation;
import com.ledger.db.entity.dto.FactoryQuotationDto;
import com.ledger.db.mapper.FactoryQuotationMapper;
import com.ledger.db.service.factory.IFactoryQuotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 成衣厂报价表 服务实现类
 * </p>
 *
 * @author ahui
 * @since 2025-10-16
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FactoryQuotationServiceImpl extends ServiceImpl<FactoryQuotationMapper, FactoryQuotation> implements IFactoryQuotationService {

    private final FactoryQuotationMapper factoryQuotationMapper;

    /**
     * 查询成衣厂报价列表 如果有条件则按条件查询
     *
     * @param factoryId   工厂ID
     * @param styleNumber 款式编号
     * @param categoryId  工作类型ID
     * @param currentPage 当前页
     * @param pageSize    页面条数
     * @param flag        状态
     * @return result
     */
    @Override
    @Transactional(readOnly = true)
    public Result<Object> queryFactoryQuotationListByCondition(
            Integer factoryId, String styleNumber, Integer categoryId,
            Integer currentPage, Integer pageSize, Integer flag) {

        // 构建分页对象
        Page<FactoryQuotationDto> page = new Page<>(currentPage, pageSize);

        factoryQuotationMapper.selectFactoryQuotationListByCondition(page, factoryId, styleNumber, categoryId, flag);

        return Result.ok(page);
    }

    /**
     * 按工厂ID查询对应工厂的报价单款式编号列表
     *
     * @param factoryId 工厂ID
     * @param flag      状态
     * @return result
     */
    @Override
    public Result<Object> queryStyleNumberListByFactoryId(Integer factoryId, Integer flag) {

        return Result.ok(
                lambdaQuery()
                        .eq(FactoryQuotation::getFactoryId, factoryId)
                        .eq(FactoryQuotation::getFlag, flag).list()
        );
    }

    /**
     * 保存成衣厂报价单信息
     *
     * @param factoryQuotation 成衣厂报价单实体
     * @return result
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Object> saveFactoryQuotationInfo(FactoryQuotation factoryQuotation) {

        // 根据成衣厂ID、款式编号、工作类型、报价来查询是否存在完全重复的项
        boolean exists = lambdaQuery()
                .eq(FactoryQuotation::getFactoryId, factoryQuotation.getFactoryId())
                .eq(StrUtil.isNotEmpty(factoryQuotation.getStyleNumber()), FactoryQuotation::getStyleNumber, factoryQuotation.getStyleNumber())
                .eq(ObjectUtil.isNotEmpty(factoryQuotation.getCategoryId()), FactoryQuotation::getCategoryId, factoryQuotation.getCategoryId())
                .exists();

        // 存在返回提示
        if (exists) {
            return Result.fail("报价信息已存在");
        }

        // 不存在则保存成衣厂报价信息
        if (save(factoryQuotation)) {
            return Result.ok();
        }

        return Result.fail();
    }

    /**
     * 更新成衣厂报价单信息
     *
     * @param factoryQuotation 成衣厂报价单实体
     * @return result
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Object> updateFactoryQuotationInfo(FactoryQuotation factoryQuotation) {

        if (updateById(factoryQuotation)) {
            return Result.ok();
        }

        return Result.fail();
    }

    /**
     * 删除成衣厂报价单信息
     *
     * @param id 成衣厂报价单ID
     * @return result
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Object> deleteFactoryQuotationById(Integer id) {

        if (removeById(id)) {
            return Result.ok();
        }

        return Result.fail();
    }
}
