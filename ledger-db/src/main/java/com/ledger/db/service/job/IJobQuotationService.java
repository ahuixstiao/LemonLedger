package com.ledger.db.service.job;

import com.ledger.db.entity.JobQuotation;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ledger.common.result.Result;

/**
 * <p>
 * 工作报价表 服务类
 * </p>
 *
 * @author ahui
 * @since 2025-10-16
 */
public interface IJobQuotationService extends IService<JobQuotation> {

    /**
     * 按条件分页查询工作报价列表。
     *
     * @param modeId 工作方式ID
     * @param categoryId 工作类型ID
     * @param currentPage 当前页
     * @param pageSize 每页条数
     * @param flag 删除状态
     * @return result
     */
    Result<Object> queryJobQuotationListByCondition(
            Integer modeId,
            Integer categoryId,
            Integer currentPage,
            Integer pageSize,
            Integer flag
    );

    /**
     * 保存工作报价。
     *
     * @param jobQuotation 报价实体
     * @return result
     */
    Result<Object> saveJobQuotationInfo(JobQuotation jobQuotation);

    /**
     * 修改工作报价。
     *
     * @param jobQuotation 报价实体
     * @return result
     */
    Result<Object> updateJobQuotationInfo(JobQuotation jobQuotation);

    /**
     * 删除工作报价。
     *
     * @param id 主键ID
     * @return result
     */
    Result<Object> removeJobQuotationInfo(Integer id);
}
