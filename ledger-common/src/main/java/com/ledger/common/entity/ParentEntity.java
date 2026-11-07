package com.ledger.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * @Author: ahui
 * @Description: TODO
 * @DateTime: 2025/10/2 - 19:17
 **/
@Data
@EqualsAndHashCode
public class ParentEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -4390488865653151243L;

    // 格式化时间 常量
    private static final String PATTERN = "yyyy-MM-dd";

    // 时区：中国标准时间是东八区，写成 GMT-8 会把日期往西偏 16 小时
    private static final String TIMEZONE = "GMT+8";

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 乐观锁
     */
    @Version
    private Integer version;

    /**
     * 创建日期
     */
    @TableField(value = "created_date", fill = FieldFill.INSERT)
    @JsonFormat(pattern = PATTERN, timezone = TIMEZONE)
    private LocalDate createdDate;

    /**
     * 最后修改日期
     *
     * <p>原来只有创建日期，同一天里改过几次完全看不出来。每次 insert/update 由
     * CustomMetaObjectHandler 自动写入，业务代码不用管。</p>
     */
    @TableField(value = "updated_date", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = PATTERN, timezone = TIMEZONE)
    private LocalDate updatedDate;

    /**
     * 逻辑删除状态 0:正常 1:禁用
     */
    @TableLogic
    private Integer flag;

    /**
     * 备注
     */
    private String remark;
}
