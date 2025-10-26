package com.ledger.db.impl.factory;

import com.ledger.common.result.Result;
import com.ledger.db.entity.FactoryBill;
import com.ledger.db.entity.dto.FactoryBillDto;
import com.ledger.db.mapper.FactoryBillMapper;
import com.ledger.db.service.factory.IFactoryBillService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 成衣厂账单表 服务实现类
 * </p>
 *
 * @author ahui
 * @since 2025-10-15
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FactoryBillServiceImpl extends ServiceImpl<FactoryBillMapper, FactoryBill> implements IFactoryBillService {

    private final FactoryBillMapper factoryBillMapper;

    /**
     * 按条件查询成衣厂账单列表  如果不传递参数则默认查询全部成衣厂账单列表
     *
     * @param factoryId   工厂ID
     * @param number      床号
     * @param styleNumber 款式编号
     * @param categoryId  工作类型
     * @param flag        删除状态 0否 1是
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @return result
     */
    @Override
    public Result<Object> queryFactoryBillListByCondition(Integer factoryId, Integer number, Integer styleNumber, Integer categoryId, Integer flag, String startDate, String endDate) {

        List<FactoryBillDto> list =
                factoryBillMapper.selectFactoryBillList(factoryId, number, styleNumber, categoryId, flag, startDate, endDate);

        if (list.isEmpty()) {
            return Result.fail("暂无数据");
        }

        return Result.ok(list);
    }
}
