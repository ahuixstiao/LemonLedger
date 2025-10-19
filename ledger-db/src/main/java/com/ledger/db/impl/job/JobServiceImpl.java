package com.ledger.db.impl.job;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.common.result.Result;
import com.ledger.db.entity.*;
import com.ledger.db.entity.dto.JobDTO;
import com.ledger.db.mapper.JobMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ledger.db.service.IEmployeeService;
import com.ledger.db.service.factory.IFactoryBillService;
import com.ledger.db.service.factory.IFactoryQuotationService;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class JobServiceImpl extends ServiceImpl<JobMapper, Job> implements IJobService {

    private final JobMapper jobMapper;

    private final IEmployeeService employeeService;

    private final IJobQuotationService jobQuotationService;

    private final IFactoryQuotationService factoryQuotationService;

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


        // 判断对象是否为空
        if (ObjectUtil.isNotNull(job)) {
            // 通过工作类型和工作方式查询工作报价表获取报价
            JobQuotation jobQuotation = jobQuotationService.lambdaQuery()
                    .eq(JobQuotation::getCategoryId, job.getCategoryId())
                    .eq(JobQuotation::getModeId, job.getModeId())
                    .one();

            // 计算工作类型和工作方式的总单价
            // 计算本条工作记录的工资 数量 * 员工工作报价
            job.setSalary(new BigDecimal(job.getQuantity()).multiply(jobQuotation.getQuotation()).setScale(2, RoundingMode.HALF_UP));

            //捕获异常
            try {
                // 判断新增工作记录是否成功
                if (save(job)) {
                    log.info("================= 工作记录保存成功: {} =================", job);
                    //新增成功后 向成衣厂账单表保存一份账单
                    FactoryBill factoryBill = new FactoryBill();        // 构建对象
                    factoryBill.setFactoryId(job.getFactoryId());       // 成衣厂
                    factoryBill.setNumber(job.getNumber());             // 床号
                    factoryBill.setStyleNumber(job.getStyleNumber());   // 款式编号
                    factoryBill.setCategoryId(job.getCategoryId());     // 工作类型
                    factoryBill.setQuantity(job.getQuantity());         // 数量
                    // 成衣厂账单字段的参数由 数量 * 成衣厂报价得出
                    // 根据员工提交的工作类型和成衣厂ID 从成衣厂报价表中获取计件的报价
                    BigDecimal quotation = factoryQuotationService.lambdaQuery()
                            .eq(FactoryQuotation::getCategoryId, job.getCategoryId())
                            .eq(FactoryQuotation::getFactoryId, job.getFactoryId())
                            .one().getQuotation();
                    // 将数量 * 成衣厂报价 得出本床账单
                    factoryBill.setBill(new BigDecimal(job.getQuantity()).multiply(quotation).setScale(2, RoundingMode.HALF_UP));

                    try {
                        // 判断账单保存是否成功
                        if (factoryBillService.save(factoryBill)) {
                            log.info("================= 成衣厂账单保存成功: {} =================", factoryBill);
                            return Result.ok();
                        }
                    } catch (RuntimeException runtimeException) {
                        log.error("成衣厂账单保存错误: {}", runtimeException.getMessage());
                    }
                }
            } catch (RuntimeException runtimeException) {
                // 打印异常
                log.error("工作信息保存错误: {}", runtimeException.getMessage());
            }

        }
        return Result.fail();
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
}
