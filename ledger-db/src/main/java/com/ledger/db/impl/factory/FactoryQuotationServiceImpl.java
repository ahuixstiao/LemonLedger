package com.ledger.db.impl.factory;

import cn.hutool.core.lang.intern.InternUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.common.result.Result;
import com.ledger.db.entity.FactoryQuotation;
import com.ledger.db.entity.dto.FactoryQuotationDto;
import com.ledger.db.mapper.FactoryQuotationMapper;
import com.ledger.db.service.factory.IFactoryQuotationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
     * @param categoryId  工作类型ID
     * @param currentPage 当前页
     * @param pageSize    页面条数
     * @return result
     */
    @Override
    public Result<Object> queryFactoryQuotationListByCondition(Integer factoryId, Integer categoryId, Integer currentPage, Integer pageSize) {

        // 构建分页对象
        Page<FactoryQuotationDto> page = new Page<>(currentPage, pageSize);

        factoryQuotationMapper.selectFactoryQuotationListBtCondition(page, factoryId, categoryId, 0);

        return Result.ok(page);
    }
}
