package com.ledger.db.impl.job;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.common.dto.employee.WorkOrderClaimDTO;
import com.ledger.common.exception.BizException;
import com.ledger.common.result.Result;
import com.ledger.common.util.MoneyUtil;
import com.ledger.db.entity.Batch;
import com.ledger.db.entity.Employee;
import com.ledger.db.entity.Job;
import com.ledger.db.entity.JobPieceRate;
import com.ledger.db.entity.JobWorkOrder;
import com.ledger.db.entity.dto.JobDTO;
import com.ledger.db.impl.LedgerServiceImpl;
import com.ledger.db.mapper.JobMapper;
import com.ledger.db.service.IBatchService;
import com.ledger.db.service.IEmployeeService;
import com.ledger.db.service.job.IJobPieceRateService;
import com.ledger.db.service.job.IJobService;
import com.ledger.db.service.job.IJobWorkOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
public class JobServiceImpl extends LedgerServiceImpl<JobMapper, Job> implements IJobService {

    private final JobMapper jobMapper;

    private final IEmployeeService employeeService;

    private final IJobPieceRateService jobPieceRateService;

    private final IJobWorkOrderService jobWorkOrderService;

    private final IBatchService batchService;

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
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> saveJobInfo(Job job) {

        // 判断对象是否为空
        if (ObjectUtil.isNull(job)) {
            throw new BizException("工作信息不能为空");
        }

        // 判断前端是否传递了 modeId
        if (ObjectUtil.isNull(job.getModeId()) || job.getModeId().equals(0)) {
            // 如果未传递则向数据库查询
            Employee employee = employeeService.lambdaQuery()
                    .eq(Employee::getId, job.getEmployeeId())
                    .one();
            if (ObjectUtil.isNull(employee)) {
                throw new BizException("员工不存在，无法确定工作方式");
            }
            job.setModeId(employee.getModeId());
        }

        // 认领工单这条路不做"完全相同记录"判重：
        // 剩余量已经由 claimWorkOrder 在行锁里校验过，同一员工同一天分几次认领同一床是正常操作
        // （上午认 50、下午再认 30）。只有手工补录才有重复提交的风险，才需要挡。
        if (ObjectUtil.isNull(job.getWorkOrderId()) && isJobDuplicate(job)) {
            throw new BizException("工作记录重复：同一员工、同一天、同一床号、同一工作方式、同一数量已经记过一条，"
                    + "如果确实要再记一次（比如分两批做），请把数量拆开分别录入");
        }

        // 手工补录（没走认领）时没有工单可继承，按 (厂, 床号, 款式) 找到或建一张床
        if (ObjectUtil.isNull(job.getWorkOrderId()) && ObjectUtil.isNull(job.getBatchId())) {
            Batch batch = batchService.findOrCreate(job.getFactoryId(), job.getNumber(), job.getStyleNumber());
            job.setBatchId(batch.getId());
        }

        // 计算工作工资
        calculateJobSalary(job);

        // 保存工作记录。
        // 这里不再 try/catch：原来的写法把异常吞成 Result.fail()，外层 @Transactional
        // 收不到异常，事务照常提交——出错时该回滚的没回滚，日志里也只有一句 message，没有堆栈。
        if (!save(job)) {
            throw new BizException("保存工作记录失败，请重试");
        }
        log.info("================= 工作记录保存成功: {} =================", job);
        return Result.ok();
    }

    /**
     * 员工认领工单并写入工作记录。
     *
     * 业务规则：
     * 1. 认领后将工单ID绑定到 Job 记录；
     * 2. 同一工单允许被多名员工认领；
     * 3. 仅在同一 modeId（工作方式）维度做防超领校验：
     *    同 modeId 下累计认领数量不能超过工单预设数量。
     *
     * 并发控制：
     * - 对工单记录加行级锁（for update），避免并发认领导致同 modeId 超领。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> claimWorkOrder(WorkOrderClaimDTO claimDTO) {
        if (ObjectUtil.isNull(claimDTO)
                || ObjectUtil.isNull(claimDTO.getWorkOrderId())
                || ObjectUtil.isNull(claimDTO.getEmployeeId())
                || ObjectUtil.isNull(claimDTO.getQuantity())) {
            throw new BizException("认领参数不完整");
        }

        if (claimDTO.getQuantity() <= 0) {
            throw new BizException("认领数量必须大于0");
        }

        JobWorkOrder workOrder = jobWorkOrderService.lambdaQuery()
                .eq(JobWorkOrder::getId, claimDTO.getWorkOrderId())
                .eq(JobWorkOrder::getFlag, 0)
                .last("for update")
                .one();

        if (ObjectUtil.isNull(workOrder)) {
            throw new BizException("工单不存在或已被删除");
        }

        Employee employee = employeeService.lambdaQuery()
                .eq(Employee::getId, claimDTO.getEmployeeId())
                .eq(Employee::getFlag, 0)
                .one();

        if (ObjectUtil.isNull(employee)) {
            throw new BizException("员工不存在或已被删除");
        }

        // 工作方式优先用本次认领显式指定的，其次退回员工档案上的默认方式。
        // 一个人可能压花、刮胶都会做（员工档案不再强制绑定唯一方式），所以认领时允许改。
        Integer claimModeId = ObjectUtil.defaultIfNull(claimDTO.getModeId(), employee.getModeId());
        if (ObjectUtil.isNull(claimModeId) || claimModeId <= 0) {
            throw new BizException("请选择本次认领的工作方式（该员工档案里没有配置默认方式）");
        }

        if (ObjectUtil.isNull(workOrder.getQuantity()) || workOrder.getQuantity() <= 0) {
            throw new BizException("工单预设数量异常，无法认领");
        }

        // 同 modeId 下的已认领数量交给数据库 SUM() 计算，不再把明细全部查回内存求和。
        // 原来 list().stream().mapToInt().sum() 的写法：一个工单认领上千次就要拉上千行，
        // 只是为了加一个数字，数据量越大越慢。
        Integer claimedQuantityOfSameMode = jobMapper.sumClaimedQuantityByMode(
                claimDTO.getWorkOrderId(), claimModeId);
        int claimedQuantity = claimedQuantityOfSameMode == null ? 0 : claimedQuantityOfSameMode;

        int remainQuantity = workOrder.getQuantity() - claimedQuantity;
        if (claimDTO.getQuantity() > remainQuantity) {
            throw new BizException("超出可认领数量，当前工作方式剩余可认领数量为: " + Math.max(remainQuantity, 0));
        }

        Job job = new Job();
        job.setEmployeeId(employee.getId());
        job.setFactoryId(workOrder.getFactoryId());
        job.setNumber(workOrder.getNumber());
        job.setStyleNumber(workOrder.getStyleNumber());
        job.setCategoryId(workOrder.getCategoryId());
        job.setQuantity(claimDTO.getQuantity());
        // 认领日期沿用工单日期，这是有意的：工资按月结算，
        // 四月认领三月的工单，这笔钱要算在三月的工资里，所以不能用 LocalDate.now()。
        job.setCreatedDate(workOrder.getCreatedDate());
        job.setModeId(claimModeId);
        job.setWorkOrderId(claimDTO.getWorkOrderId());
        // 认领的工作记录直接继承工单的床；老工单没挂床（迁移前建的）就现场补一张，
        // 否则进度、毛利这些按床汇总的口径会缺这一笔
        if (ObjectUtil.isNotNull(workOrder.getBatchId())) {
            job.setBatchId(workOrder.getBatchId());
        } else {
            job.setBatchId(batchService.findOrCreate(
                    workOrder.getFactoryId(), workOrder.getNumber(), workOrder.getStyleNumber()).getId());
        }

        // 直接调用 saveJobInfo：异常会一路往上抛，整段认领（含行锁）一起回滚。
        // 不再用「返回 Result 再判断 status」的方式——那样只能拿到失败结果，
        // 却已经把半截认领留在事务里了。
        saveJobInfo(job);

        return Result.ok("认领成功", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> updateJobInfo(Job job) {

        // 判断对象是否为空
        if (ObjectUtil.isNull(job)) {
            throw new BizException("工作信息不能为空");
        }

        // 前端可能是「只改数量/只改日期」的局部提交，缺字段时先从库里补齐，
        // 否则 calculateJobSalary 里的 job.getQuantity() 会 NPE。
        Job original = getById(job.getId());
        if (ObjectUtil.isNull(original)) {
            throw new BizException("工作记录不存在");
        }
        job.setQuantity(ObjectUtil.defaultIfNull(job.getQuantity(), original.getQuantity()));
        job.setCategoryId(ObjectUtil.defaultIfNull(job.getCategoryId(), original.getCategoryId()));
        job.setModeId(ObjectUtil.defaultIfNull(job.getModeId(), original.getModeId()));

        // 计算工作工资
        calculateJobSalary(job);

        // 更新工作记录
        if (!updateById(job)) {
            throw new BizException("修改工作记录失败：该记录不存在，或已被其他人修改，请刷新后重试");
        }
        return Result.ok();
    }

    /**
     * 删除工作信息
     *
     * @param jobId 工作ID
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> deleteJobInfo(Integer jobId) {

        lambdaQuery().eq(Job::getId, jobId).oneOpt().orElseThrow(() -> new BizException("找不到对应的工作记录"));

        // 删除工作记录
        if (!removeById(jobId)) {
            throw new BizException("删除工作记录失败，请重试");
        }

        log.info("================= 工作信息删除成功，JobID为: {} =================", jobId);
        return Result.ok();
    }

    /**
     * 判断该工作记录是否重复存在。
     *
     * <p>维度里包含"数量"：同一员工同一天在同一床上做两次、数量分别是 50 和 30，是正常操作；
     * 只有连数量都一模一样（典型的重复点击保存）才拦。原来不带数量，第二次录入必被拦死。</p>
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
                .eq(Job::getQuantity, job.getQuantity())
                .eq(Job::getCreatedDate, job.getCreatedDate())
                .eq(Job::getFlag, 0)
                .exists();
    }

    /**
     * 计算工作工资
     *
     * @param job 工作记录
     */
    private void calculateJobSalary(Job job) {

        if (ObjectUtil.isNull(job.getQuantity())) {
            throw new BizException("数量不能为空，无法计算工资");
        }

        // 通过工作类型和工作方式查询员工计件单价表获取计件单价
        JobPieceRate jobPieceRate = jobPieceRateService.lambdaQuery()
                .eq(JobPieceRate::getCategoryId, job.getCategoryId())
                .eq(JobPieceRate::getModeId, job.getModeId())
                .oneOpt()
                .orElseThrow(() -> new BizException("找不到对应的员工计件单价信息（工作类型 + 工作方式）"));

        if (jobPieceRate.getPrice() == null) {
            throw new BizException("该员工计件单价没有填写单价，无法计算工资");
        }

        // 计算本条工作记录的工资：数量 × 员工计件单价，金额统一保留两位小数（四舍五入）
        job.setSalary(MoneyUtil.scale(
                BigDecimal.valueOf(job.getQuantity()).multiply(jobPieceRate.getPrice())
        ));

    }
}
