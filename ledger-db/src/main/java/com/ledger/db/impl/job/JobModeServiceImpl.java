package com.ledger.db.impl.job;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ledger.common.result.Result;
import com.ledger.db.entity.JobMode;
import com.ledger.db.mapper.JobModeMapper;
import com.ledger.db.service.job.IJobModeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 工作方式表 服务实现类
 * </p>
 *
 * @author ahui
 * @since 2025-10-02
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class JobModeServiceImpl extends ServiceImpl<JobModeMapper, JobMode> implements IJobModeService {

    /**
     * 查询工作方式列表
     *
     * @param currentPage 当前页
     * @param pageSize    页面条数
     * @param mode        工作方式
     * @return result
     */
    @Override
    public Result<Object> queryJobModeList(Integer currentPage, Integer pageSize, String mode, Integer flag) {

        Page<JobMode> page = new Page<>(currentPage, pageSize);
        String queryMode = StrUtil.trim(mode);
        Integer queryFlag = flag == null ? 0 : flag;

        Page<JobMode> result = baseMapper.queryJobModeList(page, queryMode, queryFlag);
        return Result.ok(result);
    }

    /**
     * 保存工作方式信息
     *
     * @param jobMode 工作方式实体
     * @return result
     */
    @Override
    public Result<Object> saveJobModeInfo(JobMode jobMode) {

        if (save(jobMode)) {
            return Result.ok();
        }

        return Result.fail();
    }

    /**
     * 删除工作方式信息
     *
     * @param jobModeId 工作方式ID
     * @return result
     */
    @Override
    public Result<Object> removeJobModeInfo(Integer jobModeId) {

        if (removeById(jobModeId)) {
            return Result.ok();
        }

        return Result.fail();
    }
}
