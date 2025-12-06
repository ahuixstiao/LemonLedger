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

    // 时区
    private static final String TIMEZONE = "GMT-8";

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
     * 逻辑删除状态 0:正常 1:禁用
     */
    @TableLogic
    private Integer flag;

    /**
     * 备注
     */
    private String remark;
}
