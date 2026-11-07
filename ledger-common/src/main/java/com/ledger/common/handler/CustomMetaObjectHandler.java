package com.ledger.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * @Author: ahui
 * @Description: MybatisPlus 公共字段自动填充处理器
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
        // 不打 log.info：每插一条数据打一行日志，账目多了之后日志会被刷爆
        log.debug("插入操作，自动填充 createdDate / updatedDate");
        this.strictInsertFill(metaObject, "createdDate", LocalDate::now, LocalDate.class);
        this.strictInsertFill(metaObject, "updatedDate", LocalDate::now, LocalDate.class);
    }

    /**
     * 更新元对象字段填充（用于更新时对公共字段的填充）
     *
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 每次更新都覆盖成今天：用于事后判断这条记录最近有没有被改过
        // strictUpdateFill 只在字段为 null 时填充，所以先强制置空
        this.setFieldValByName("updatedDate", null, metaObject);
        this.strictUpdateFill(metaObject, "updatedDate", LocalDate::now, LocalDate.class);
    }
}
