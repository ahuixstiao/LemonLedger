package com.ledger.db.impl.job;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ledger.common.result.Result;
import com.ledger.db.entity.Employee;
import com.ledger.db.entity.FactoryBill;
import com.ledger.db.entity.Job;
import com.ledger.db.entity.JobQuotation;
import com.ledger.db.entity.dto.JobDTO;
import com.ledger.db.mapper.JobMapper;
import com.ledger.db.service.IEmployeeService;
import com.ledger.db.service.factory.IFactoryBillService;
import com.ledger.db.service.job.IJobQuotationService;
import com.ledger.db.service.job.IJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * <p>
 * 工作内容表 服务实现类
 * </p>
 *
 * @author ahui
 * @since 2025-10-02
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class JobServiceImpl extends ServiceImpl<JobMapper, Job> implements IJobService {

    private final JobMapper jobMapper;

    private final IEmployeeService employeeService;

    private final IJobQuotationService jobQuotationService;

    private final IFactoryBillService factoryBillService;

    /**
     * 默认查询当天所有员工的工作信息
     * 如果传入员工ID 则按员工ID 查询当天 或 由员工指定的起止日期的工作信息
     *
     * @param employeeId  员工ID
     * @param startDate   开始时间
     * @param endDate     结束时间
     * @param factoryId   指定工厂
     * @param number      指定床号
     * @param categoryId  指定工作类型
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @param flag        删除状态 0否 1是
     * @return result
     */
    @Override
    @Transactional(readOnly = true)
    public Result<Object> queryJobListByEmployeeIDAndDate(
            Integer employeeId, String startDate, String endDate,
            Integer factoryId, String number, Integer categoryId,
            Integer currentPage, Integer pageSize, Integer flag) {

        //根据员工ID查询员工工作信息 假如传入时间为空则查询当天
        if (StrUtil.isBlank(startDate)) {
            startDate = LocalDate.now().toString();
        }
        // 用户如果指定时间范围则按范围查询
        if (StrUtil.isNotBlank(startDate) & StrUtil.isNotBlank(endDate)) {
            // 格式化前端传入的时间 YYYY-MM-DD
            startDate = LocalDate.parse(startDate).format(DateTimeFormatter.ISO_DATE);
            endDate = LocalDate.parse(endDate).format(DateTimeFormatter.ISO_DATE);
        }

        // 构建分页查询对象
        Page<JobDTO> page = new Page<>(currentPage, pageSize);

        // 查询
        page = jobMapper.selectJobListByCondition(page,
                employeeId, startDate, endDate,
                factoryId, number, categoryId,
                flag);

        // 返回结果
        return Result.ok(page);
    }

    /**
     * 统计员工薪水
     *
     * @param employeeId 员工ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param flag       删除状态 0否 1是
     * @return result
     */
    @Override
    @Transactional(readOnly = true)
    public Result<Object> statisticalSalary(Integer employeeId, String startTime, String endTime, Integer flag) {

        // 统计员工薪资
        JobDTO jobDTO = jobMapper.calculateSalaryByEmployeeIdAndDate(employeeId, startTime, endTime, flag);

        if (ObjectUtil.isNotEmpty(jobDTO)) {
            return Result.ok(jobDTO);
        }

        return Result.fail("暂无信息");
    }

    /**
     * 员工添加工作记录
     *
     * @param job 工作记录
     * @return result
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Object> saveJobInfo(Job job) {

        // 判断对象是否为空
        if (ObjectUtil.isNull(job)) {
            return Result.fail("工作信息不能为空");
        }

        // 判断是否存在相同的工作信息记录, 不存在才进行下一步操作
        if (isJobDuplicate(job)) {
            return Result.fail("工作记录重复");
        }

        // 判断前端是否传递了 modeId
        if(ObjectUtil.isNull(job.getModeId()) || job.getModeId().equals(0)) {
            // 如果未传递则向数据库查询
            job.setModeId(
                    employeeService.lambdaQuery()
                            .eq(Employee::getId, job.getEmployeeId())
                            .one()
                            .getModeId()
            );
        }

        // 计算工作工资
        calculateJobSalary(job);

        //捕获异常
        try {
            // 保存 工作记录
            if (save(job)) {
                log.info("================= 工作记录保存成功: {} =================", job);
            }else {
                log.info("================= 工作记录保存失败: {} =================", job);
            }

        } catch (RuntimeException runtimeException) {
            // 打印异常
            log.error("错误: {}", runtimeException.getMessage());
            return Result.fail();
        }
        return Result.ok();
    }

    /**
     * 更新工作信息
     * @param job 工作记录
     * @return result
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Object> updateJobInfo(Job job) {

        // 判断对象是否为空
        if (ObjectUtil.isNull(job)) {
            return Result.fail("工作信息不能为空");
        }

        // 计算工作工资
        calculateJobSalary(job);

        // 更新工作记录
        if (updateById(job)) {
            return Result.ok();
        }

        return Result.fail();
    }

    /**
     * 删除工作信息
     *
     * @param jobId 工作ID
     * @return result
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Object> deleteJobInfo(Integer jobId) {

        Job job = lambdaQuery().eq(Job::getId, jobId).oneOpt().orElseThrow(() -> new RuntimeException("找不到对应的工作记录"));

        // 删除工作记录的同时也删除成衣厂账单记录
        if (removeById(jobId)) {
            log.info("================= 工作信息删除成功，JobID为: {} =================", jobId);

            // 构建删除条件
            Wrapper<FactoryBill> wrapper = new QueryWrapper<FactoryBill>()
                    .eq("factory_id", job.getFactoryId())
                    .eq("number", job.getNumber())
                    .eq("style_number", job.getStyleNumber())
                    .eq("category_id", job.getCategoryId())
                    .eq("created_date", job.getCreatedDate());
            log.info("================= 构建删除成衣厂账单条件 {} =================", wrapper.getEntity());
            boolean exists = factoryBillService.remove(wrapper);
            if (exists) {
                log.info("================= 成衣厂账单删除成功 =================");
            } else {
                log.info("================= 成衣厂账单删除失败 =================");
            }
            return Result.ok();
        }

        log.info("================= 工作信息删除失败，JobID: {} =================", jobId);

        return Result.fail();
    }

    /**
     * 判断该工作记录是否重复存在 以员工ID、工厂ID、工作类别ID、工作方式ID、日期来判断
     *
     * @param job 工作信息
     * @return boolean
     */
    private boolean isJobDuplicate(Job job) {

        return lambdaQuery()
                .eq(Job::getEmployeeId, job.getEmployeeId())
                .eq(Job::getFactoryId, job.getFactoryId())
                .eq(Job::getCategoryId, job.getCategoryId())
                .eq(Job::getStyleNumber, job.getStyleNumber())
                .eq(Job::getNumber, job.getNumber())
                .eq(Job::getModeId, job.getModeId())
                .eq(Job::getCreatedDate, job.getCreatedDate())
                .eq(Job::getFlag, 0)
                .exists();
    }

    /**
     * 计算工作工资
     *
     * @param job
     */
    private void calculateJobSalary(Job job) {

        // 通过工作类型和工作方式查询工作报价表获取报价
        BigDecimal quotation = jobQuotationService.lambdaQuery()
                .eq(JobQuotation::getCategoryId, job.getCategoryId())
                .eq(JobQuotation::getModeId, job.getModeId())
                .oneOpt().orElseThrow(() -> new RuntimeException("找不到对应的工作报价信息")).getQuotation();

        // 计算本条工作记录的工资 数量 * 工作报价
        job.setSalary(BigDecimal
                .valueOf(job.getQuantity())
                .multiply(quotation)
                .setScale(2, RoundingMode.HALF_UP)
        );

    }
}
