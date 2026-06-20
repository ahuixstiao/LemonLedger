package com.ledger.db.util;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ledger.db.entity.FactoryBill;

import java.time.LocalDate;
import java.util.List;

/**
 * 账单重复校验工具类。
 */
public class BillDuplicateCheckUtil {

    /**
     * 检查指定账单维度在所选日期前后20天内是否存在重复账单，并返回重复日期提示。
     *
     * @param billService   账单查询服务
     * @param factoryId     工厂ID
     * @param number        床号
     * @param quotationId   报价ID
     * @param selectedDate  所选日期
     * @param excludeBillId 需要排除的账单ID，新增时传 null，修改时传当前账单ID
     * @return 重复日期提示；若无重复返回空字符串
     */
    public static String findDuplicateBillDatesWithinTwentyDays(
            IService<FactoryBill> billService,
            Integer factoryId,
            String number,
            Integer quotationId,
            LocalDate selectedDate,
            Integer excludeBillId
    ) {
        if (ObjectUtil.isNull(billService) || ObjectUtil.isNull(factoryId) || StrUtil.isBlank(number)
                || ObjectUtil.isNull(quotationId) || ObjectUtil.isNull(selectedDate)) {
            return StrUtil.EMPTY;
        }

        LocalDate startDate = selectedDate.minusDays(20);
        LocalDate endDate = selectedDate.plusDays(20);

        var query = billService.lambdaQuery()
                .eq(FactoryBill::getFactoryId, factoryId)
                .eq(FactoryBill::getNumber, number)
                .eq(FactoryBill::getQuotationId, quotationId)
                .between(FactoryBill::getCreatedDate, startDate, endDate)
                .eq(FactoryBill::getFlag, 0);

        if (ObjectUtil.isNotNull(excludeBillId)) {
            query.ne(FactoryBill::getId, excludeBillId);
        }

        List<FactoryBill> duplicateBills = query.list();
        if (ObjectUtil.isEmpty(duplicateBills)) {
            return StrUtil.EMPTY;
        }

        return duplicateBills.stream()
                .map(FactoryBill::getCreatedDate)
                .filter(ObjectUtil::isNotNull)
                .distinct()
                .sorted()
                .map(LocalDate::toString)
                .reduce((left, right) -> left + "、" + right)
                .orElse(StrUtil.EMPTY);
    }
}
