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
     * 创建人
     */
    @TableField(value = "created_by", fill = FieldFill.INSERT) // 创建时自动填充
    private String createdBy;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = PATTERN, timezone = TIMEZONE)
    private LocalDate createdTime;

    /**
     * 修改人
     */
    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    /**
     * 修改时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = PATTERN, timezone = TIMEZONE)
    private LocalDate updatedTime;

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
