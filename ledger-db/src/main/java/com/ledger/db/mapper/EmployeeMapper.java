package com.ledger.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.db.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 员工表 Mapper 接口
 * </p>
 *
 * @author ahui
 * @since 2025-10-02
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

    /**
     * 按条件分页查询员工列表（绕过逻辑删除自动条件）
     *
     * @param page 分页参数
     * @param name 员工名称
     * @param flag 删除状态 0否 1是
     * @return 分页结果
     */
    Page<Employee> queryEmployeeList(Page<Employee> page,
                                      @Param("name") String name,
                                      @Param("flag") Integer flag);
}
