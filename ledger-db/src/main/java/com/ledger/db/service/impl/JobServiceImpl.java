package com.ledger.db.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.common.result.Result;
import com.ledger.common.util.ConvertUtil;
import com.ledger.db.entity.Employee;
import com.ledger.db.entity.Job;
import com.ledger.db.entity.JobCategory;
import com.ledger.db.entity.dto.JobDTO;
import com.ledger.db.mapper.JobMapper;
import com.ledger.db.service.IEmployeeService;
import com.ledger.db.service.IJobCategoryService;
import com.ledger.db.service.IJobService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <p>
 * 工作内容表 服务实现类
 * </p>
 *
 * @author ahui
 * @since 2025-10-02
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class JobServiceImpl extends ServiceImpl<JobMapper, Job> implements IJobService {

    private final IEmployeeService employeeService;

    private final JobMapper jobMapper;

    /**
     * 查询员工当天或某天的工作信息
     *
     * @param employeeId  员工id
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @return result
     */
    @Override
    public Result<Object> queryJobByDate(
            Integer employeeId,
            String startDate, String endDate,
            Integer currentPage, Integer pageSize) {

        //根据员工ID查询员工工作信息 假如传入时间为空则查询当天
        if (StrUtil.isBlank(startDate)) {
            startDate = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE);
        }
        // 用户如果指定时间范围则按范围查询
        if (StrUtil.isNotBlank(startDate) & StrUtil.isNotBlank(endDate)) {
            // 格式化前端传入的时间 yyyy-mm-dd
            startDate = LocalDateTime.parse(startDate).format(DateTimeFormatter.ISO_DATE);
            endDate = LocalDateTime.parse(endDate).format(DateTimeFormatter.ISO_DATE);
        }

        // 构建分页查询对象
        Page<JobDTO> page = jobMapper.selectJobListByEmployeeId(
                new Page<>(currentPage, pageSize), employeeId, startDate, endDate, 0
        );

        // 返回结果
        return Result.ok(page);
    }

    /**
     * 员工添加工作记录
     *
     * @param job 工作记录
     * @return result
     */
    @Override
    public Result<Object> addJobRecord(Job job) {

        if (save(job)) {
            return Result.ok();
        }

        return Result.fail();
    }

    /**
     * @param job 工作记录
     * @return result
     */
    @Override
    public Result<Object> updateJobRecord(Job job) {

        if (updateById(job)) {
            return Result.ok();
        }

        return Result.fail();
    }
}
