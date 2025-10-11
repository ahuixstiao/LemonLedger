package com.ledger.db.service.impl;

import cn.hutool.core.util.ObjectUtil;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

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

    private final JobMapper jobMapper;

    private final IJobCategoryService jobCategoryService;


    /**
     * 默认查询当天所有员工的工作信息
     *
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @return result
     */
    @Override
    public Result<Object> queryJobListByDefaultCurrentDay(Integer currentPage, Integer pageSize) {

        // 构建分页对象
        Page<JobDTO> page = new Page<>(
                Optional.ofNullable(currentPage).orElse(1),
                Optional.ofNullable(pageSize).orElse(5));

        // 查询
        page = jobMapper.selectJobListByDefaultCurrentDay(page, 0);

        return Result.ok(page);
    }

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
    public Result<Object> queryJobListByEmployeeIDAndDate(
            Integer employeeId,
            String startDate, String endDate,
            Integer currentPage, Integer pageSize) {

        //根据员工ID查询员工工作信息 假如传入时间为空则查询当天
        if (StrUtil.isBlank(startDate)) {
            startDate = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE);
        }
        // 用户如果指定时间范围则按范围查询
        if (StrUtil.isNotBlank(startDate) & StrUtil.isNotBlank(endDate)) {
            // 格式化前端传入的时间 YYYY-MM-DD
            startDate = LocalDate.parse(startDate).format(DateTimeFormatter.ISO_DATE);
            endDate = LocalDate.parse(endDate).format(DateTimeFormatter.ISO_DATE);
        }

        // 构建分页查询对象
        Page<JobDTO> page = new Page<>(
                Optional.ofNullable(currentPage).orElse(1),
                Optional.ofNullable(currentPage).orElse(5));

        // 查询
        page = jobMapper.selectJobListByEmployeeId(page, employeeId, startDate, endDate, 0);

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
        // 先区分 该员工是什么工种 再计算工作薪资
        // 查工作类型表获取对应工种工价
        BigDecimal price = jobCategoryService.lambdaQuery().eq(JobCategory::getModeId, job.getModeId()).one().getPrice();
        // 计算本条工作记录的工资 数量 * 工作类型和工作方式的单价
        job.setSalary(new BigDecimal(job.getQuantity()).multiply(price).setScale(2, RoundingMode.HALF_UP));

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

        // 判断是否修改数量或者工作类型和方式 如果有修改则需要重新计算工资
        if (job.getModeId() != null || job.getQuantity() != null) {

        }

        if (updateById(job)) {
            return Result.ok();
        }

        return Result.fail();
    }
}
