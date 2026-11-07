package com.ledger.db.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ledger.common.entity.ParentEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>
 * 床（一个成衣厂 + 一个床号 + 一个款式编号 = 一张床）
 * </p>
 * <p>
 * 以前工单、账单、工作记录各自把「厂 + 床号 + 款式」抄一遍，彼此没有主键级关联：
 * 床号写错一个字母，三张单据就对不上了，也查不出一床布到底赚不赚钱。
 * 现在三张单据都挂 batch_id，改床号只改 batch 一处，再由服务层同步冗余列。
 * </p>
 *
 * @author ahui
 * @since 2026-11-06
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("batch")
public class Batch extends ParentEntity {

    @Serial
    private static final long serialVersionUID = 4487152947361087031L;

    /**
     * 成衣厂ID
     */
    private Integer factoryId;

    /**
     * 床号
     */
    private String number;

    /**
     * 款式编号
     */
    private String styleNumber;
}
