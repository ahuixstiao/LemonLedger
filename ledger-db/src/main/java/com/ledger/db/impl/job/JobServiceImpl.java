package com.ledger.db.impl.job;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ledger.common.result.Result;
import com.ledger.db.entity.FactoryBill;
import com.ledger.db.entity.Job;
import com.ledger.db.entity.JobQuotation;
import com.ledger.db.entity.dto.JobDTO;
import com.ledger.db.mapper.JobMapper;
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

    private final IJobQuotationService jobQuotationService;

    private final IFactoryBillService factoryBillService;

    /**
     * 默认查询当天所有员工的工作信息
     * 如果传入员工ID 则按员工ID 查询当天 或 由员工指定的起止日期的工作信息
     *
     * @param employeeId  员工ID
     * @param startDate   开始时间
     * @param endDate     结束时间
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @param flag        删除状态 0否 1是
     * @return result
     */
    @Override
    @Transactional(readOnly = true)
    public Result<Object> queryJobListByEmployeeIDAndDate(
            Integer employeeId, String startDate, String endDate,
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
        page = jobMapper.selectJobListByEmployeeId(page, employeeId, startDate, endDate, flag);

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

        // 重复问题： 员工可以提交多条相同的记录 保存之前要做查询处理存在则不允许保存实现去重
        // 工厂账单问题多个员工提交同一份工作记录时会出现多份相同账单，也要做去重处理 
        // 比如A和B是搭档做的是同一床，那么他们两个就会提交一份同样的工作记录，导致成衣厂账单出现两份相同的
        // 由于员工的工作类型和成衣厂账单的工作类型需求不一致所以需要后台手动填写成衣厂账单的工作类型才能得出账单价格

        // 判断对象是否为空
        if (ObjectUtil.isNull(job)) {
            return Result.fail("工作信息不能为空");
        }

        // 判断是否存在相同的工作信息记录, 不存在才进行下一步操作
        if (isJobDuplicate(job)) {
            return Result.fail("工作记录重复");
        }

        // 计算工作工资
        calculateJobSalary(job);

        //捕获异常
        try {
            // 保存 工作记录
            save(job);
            // 保存 成衣厂账单
            saveFactoryBillIfNotExists(job);
            log.info("================= 工作记录保存成功: {} =================", job);
        } catch (RuntimeException runtimeException) {
            // 打印异常
            log.error("错误: {}", runtimeException.getMessage());
            return Result.fail();
        }
        return Result.ok();
    }

    /**
     * @param job 工作记录
     * @return result
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Object> updateJobInfo(Job job) {

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

        if (removeById(jobId)) {
            return Result.ok();
        }

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
                .eq(Job::getCreatedTime, LocalDate.now())
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
        JobQuotation jobQuotation = jobQuotationService.lambdaQuery()
                .eq(JobQuotation::getCategoryId, job.getCategoryId())
                .eq(JobQuotation::getModeId, job.getModeId())
                .oneOpt().orElseThrow(() -> new RuntimeException("找不到对应的工作报价"));

        // 计算本条工作记录的工资 数量 * 工作报价
        job.setSalary(BigDecimal
                .valueOf(job.getQuantity())
                .multiply(jobQuotation.getQuotation())
                .setScale(2, RoundingMode.HALF_UP)
        );

    }

    /**
     * 判断该成衣厂账单是否存在重复 以工厂ID、床号、款式编号、日期来判断
     *
     * @param job 工作信息
     * @return boolean
     */
    private boolean isFactoryBillDuplicate(Job job) {

        return factoryBillService.lambdaQuery()
                .eq(FactoryBill::getFactoryId, job.getFactoryId())
                .eq(FactoryBill::getNumber, job.getNumber())
                .eq(FactoryBill::getStyleNumber, job.getStyleNumber())
                .eq(FactoryBill::getCreatedTime, LocalDate.now())
                .eq(FactoryBill::getFlag, 0)
                .exists();
    }

    /**
     * 保存成衣厂账单信息
     *
     * @param job 工作信息
     */
    private void saveFactoryBillIfNotExists(Job job) {

        // 判断是否已经存在相同的成衣厂账单
        if (isFactoryBillDuplicate(job)) {
            return;
        }

        //构建 成衣厂账单对象
        FactoryBill factoryBill = new FactoryBill();        // 构建对象
        factoryBill.setFactoryId(job.getFactoryId());       // 成衣厂
        factoryBill.setNumber(job.getNumber());             // 床号
        factoryBill.setStyleNumber(job.getStyleNumber());   // 款式编号
        factoryBill.setQuantity(job.getQuantity());         // 数量
        // 成衣厂账单参数 由管理员手动在后台中选择工作类型后得出

        factoryBillService.save(factoryBill);

        log.info("================= 成衣厂账单保存成功: {} =================", factoryBill);
    }

}
