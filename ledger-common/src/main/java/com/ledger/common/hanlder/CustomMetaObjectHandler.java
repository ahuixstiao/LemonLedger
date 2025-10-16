package com.ledger.common.hanlder;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * @Author: ahui
 * @Description: TODO MybatisPlus 字段填充处理器
 * @DateTime: 2025/10/16 - 20:26
 **/
@Slf4j
@Component
public class CustomMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入元对象字段填充（用于插入时对公共字段的填充）
     *
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info(">>>>>>>>>>>>>>>>> 开始插入填充字段 >>>>>>>>>>>>>>>>>");
        // 创建时间
        this.strictInsertFill(metaObject, "createdTime", LocalDate::now, LocalDate.class);
    }

    /**
     * 更新元对象字段填充（用于更新时对公共字段的填充）
     *
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info(">>>>>>>>>>>>>>>>> 开始更新填充字段 >>>>>>>>>>>>>>>>>");
        // 修改时间
        this.strictUpdateFill(metaObject, "updatedTime", LocalDate::now, LocalDate.class);
    }
}
