package com.ledger.db.service.job;

import com.ledger.common.result.Result;
import com.ledger.db.entity.JobMode;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 工作方式表 服务类
 * </p>
 *
 * @author ahui
 * @since 2025-10-02
 */
public interface IJobModeService extends IService<JobMode> {

    /**
     * 查询工作方式列表
     *
     * @param currentPage 当前页
     * @param pageSize    页面条数
     * @param mode        工作方式
     * @return result
     */
    Result<Object> queryJobModeList(Integer currentPage, Integer pageSize, String mode);

    /**
     * 保存工作方式信息
     *
     * @param jobMode 工作方式实体
     * @return result
     */
    Result<Object> saveJobModeInfo(JobMode jobMode);

    /**
     * 删除工作方式信息
     *
     * @param jobModeId 工作方式ID
     * @return result
     */
    Result<Object> removeJobModeInfo(Integer jobModeId);

}
