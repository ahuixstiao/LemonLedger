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
     * 检查指定账单维度在所选日期前后 windowDays 天内是否存在重复账单，并返回重复日期提示。
     *
     * <p>窗口天数来自配置（ledger.bill.duplicate-window-days），不再写死 20：
     * 改一次配置就能调整松紧，返工、补做这类确实需要重复记账的情况由调用方用"确认后强制保存"放行。</p>
     *
     * @param billService   账单查询服务
     * @param factoryId     工厂ID
     * @param number        床号
     * @param pieceRateId   计件单价ID
     * @param selectedDate  所选日期
     * @param excludeBillId 需要排除的账单ID，新增时传 null，修改时传当前账单ID
     * @param windowDays    前后各查多少天
     * @return 重复日期提示；若无重复返回空字符串
     */
    public static String findDuplicateBillDates(
            IService<FactoryBill> billService,
            Integer factoryId,
            String number,
            Integer pieceRateId,
            LocalDate selectedDate,
            Integer excludeBillId,
            int windowDays
    ) {
        if (ObjectUtil.isNull(billService) || ObjectUtil.isNull(factoryId) || StrUtil.isBlank(number)
                || ObjectUtil.isNull(pieceRateId) || ObjectUtil.isNull(selectedDate) || windowDays < 0) {
            return StrUtil.EMPTY;
        }

        LocalDate startDate = selectedDate.minusDays(windowDays);
        LocalDate endDate = selectedDate.plusDays(windowDays);

        var query = billService.lambdaQuery()
                .eq(FactoryBill::getFactoryId, factoryId)
                .eq(FactoryBill::getNumber, number)
                .eq(FactoryBill::getPieceRateId, pieceRateId)
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
