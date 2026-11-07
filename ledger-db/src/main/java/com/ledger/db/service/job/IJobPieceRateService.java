package com.ledger.db.service.job;

import com.ledger.db.entity.JobPieceRate;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ledger.common.result.Result;

/**
 * <p>
 * 员工计件单价表 服务类
 * </p>
 *
 * @author ahui
 * @since 2025-10-16
 */
public interface IJobPieceRateService extends IService<JobPieceRate> {

    /**
     * 按条件分页查询员工计件单价列表。
     *
     * @param modeId 工作方式ID
     * @param categoryId 工作类型ID
     * @param currentPage 当前页
     * @param pageSize 每页条数
     * @param flag 删除状态
     * @return result
     */
    Result<Object> queryJobPieceRateListByCondition(
            Integer modeId,
            Integer categoryId,
            Integer currentPage,
            Integer pageSize,
            Integer flag
    );

    /**
     * 保存员工计件单价。
     *
     * @param jobPieceRate 计件单价实体
     * @return result
     */
    Result<Object> saveJobPieceRateInfo(JobPieceRate jobPieceRate);

    /**
     * 修改员工计件单价。
     *
     * @param jobPieceRate 计件单价实体
     * @return result
     */
    Result<Object> updateJobPieceRateInfo(JobPieceRate jobPieceRate);

    /**
     * 删除员工计件单价。
     *
     * @param id 主键ID
     * @return result
     */
    Result<Object> removeJobPieceRateInfo(Integer id);
}
