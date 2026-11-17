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
import com.ledger.db.mapper.JobWorkOrderMapper;
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

    private final JobWorkOrderMapper jobWorkOrderMapper;

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
     * 员工添加工作记录（手工补录）
     *
     * <p>认领工单不能走这里：认领必须经过 {@link #claimWorkOrder(WorkOrderClaimDTO)}，
     * 由它先原子扣减工单上的已认领数量。以前这里对带 workOrderId 的请求跳过了判重，
     * 又不校验剩余量，等于给了一条绕过认领校验的旁路，同一张工单能被反复“补录”成超领。</p>
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

        if (ObjectUtil.isNotNull(job.getWorkOrderId())) {
            throw new BizException("认领工单请使用认领接口，不能直接保存带工单ID的工作记录");
        }

        return saveJobRecord(job);
    }

    /**
     * 真正落库的工作记录写入，认领与手工补录共用。
     *
     * @param job 工作记录（认领时已由调用方扣减过工单计数）
     * @return result
     */
    private Result<Object> saveJobRecord(Job job) {

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

        // 手工补录要挡「同一员工、同一天、同一床号、同一工作方式、同一数量」的重复提交；
        // 认领记录不在此列（同一员工同一天分几次认领同一床是正常的，上午 50、下午 30）。
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
     * - 「剩余够不够」的判断与扣减在同一条 UPDATE 里完成（见 JobWorkOrderMapper
     *   increaseClaimedEmboss / increaseClaimedGlue），靠 InnoDB 行锁串行化。
     *   以前是「先 SUM 查已认领、再插入」，两个并发请求都能读到同一个剩余量，
     *   于是同时通过校验、双双插入，造成同一张工单被超领。
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

        // 这里只读工单本身（不锁）：真正的并发争抢发生在下面的原子 UPDATE 上，
        // 由它持有工单行的写锁，谁先拿到谁先扣。
        JobWorkOrder workOrder = jobWorkOrderService.lambdaQuery()
                .eq(JobWorkOrder::getId, claimDTO.getWorkOrderId())
                .eq(JobWorkOrder::getFlag, 0)
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

        // 判断 + 扣减合并成一条 UPDATE：只有当「数量 - 该工种已认领」还放得下本次认领时才会命中行，
        // 并发过来的第二个请求会被行锁挡住，拿到行后重新判断，放不下就影响 0 行。
        // 压花、刮胶是一床布的两道工序，各自有一份数量，所以按 mode 分别计数。
        int claimedRows = increaseClaimedQuantity(workOrder.getId(), claimModeId, claimDTO.getQuantity());
        if (claimedRows == 0) {
            // 重新读一次拿最新的剩余量，报错信息才对得上（刚才的快照可能已经过期）
            JobWorkOrder latest = jobWorkOrderService.getById(workOrder.getId());
            int remainQuantity = ObjectUtil.isNull(latest) ? 0 : Math.max(remainingQuantity(latest, claimModeId), 0);
            throw new BizException("超出可认领数量，当前工作方式剩余可认领数量为: " + remainQuantity);
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
        saveJobRecord(job);

        return Result.ok("认领成功", null);
    }

    /**
     * 工作方式：1 = 压花
     */
    private static final Integer CLAIM_MODE_EMBOSS = 1;

    /**
     * 工作方式：2 = 刮胶
     */
    private static final Integer CLAIM_MODE_GLUE = 2;

    /**
     * 按工作方式给工单的已认领数量加数，返回 0 表示剩余不足/工单已删除。
     *
     * @param workOrderId 工单ID
     * @param modeId      工作方式
     * @param quantity    本次数量（正数）
     * @return 影响行数
     */
    private int increaseClaimedQuantity(Integer workOrderId, Integer modeId, Integer quantity) {
        if (CLAIM_MODE_EMBOSS.equals(modeId)) {
            return jobWorkOrderMapper.increaseClaimedEmboss(workOrderId, quantity);
        }
        if (CLAIM_MODE_GLUE.equals(modeId)) {
            return jobWorkOrderMapper.increaseClaimedGlue(workOrderId, quantity);
        }
        throw new BizException("不支持的工作方式：" + modeId);
    }

    /**
     * 按工作方式给工单的已认领数量减数（兜底不小于 0）。
     *
     * @param workOrderId 工单ID
     * @param modeId      工作方式
     * @param quantity    要减掉的数量（正数）
     */
    private void decreaseClaimedQuantity(Integer workOrderId, Integer modeId, Integer quantity) {
        if (ObjectUtil.isNull(workOrderId) || ObjectUtil.isNull(quantity) || quantity <= 0) {
            return;
        }
        if (CLAIM_MODE_EMBOSS.equals(modeId)) {
            jobWorkOrderMapper.decreaseClaimedEmboss(workOrderId, quantity);
            return;
        }
        if (CLAIM_MODE_GLUE.equals(modeId)) {
            jobWorkOrderMapper.decreaseClaimedGlue(workOrderId, quantity);
            return;
        }
        throw new BizException("不支持的工作方式：" + modeId);
    }

    /**
     * 某个工种当前还剩多少可认领（按传入工单的快照算，只用于报错提示）
     *
     * @param workOrder 工单
     * @param modeId    工作方式
     * @return 剩余量
     */
    private int remainingQuantity(JobWorkOrder workOrder, Integer modeId) {
        Integer claimed = CLAIM_MODE_EMBOSS.equals(modeId) ? workOrder.getClaimedEmboss() : workOrder.getClaimedGlue();
        int claimedQuantity = claimed == null ? 0 : claimed;
        int total = workOrder.getQuantity() == null ? 0 : workOrder.getQuantity();
        return total - claimedQuantity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> updateJobInfo(Job job) {

        // 判断对象是否为空
        if (ObjectUtil.isNull(job)) {
            throw new BizException("工作信息不能为空");
        }

        // 锁住这条工作记录：下面要按新旧数量的差额调整工单上的已认领计数，
        // 两个请求同时改同一条记录时，没有这把锁会各自读到同一份旧值、把差额重复算一遍。
        Job original = lambdaQuery()
                .eq(Job::getId, job.getId())
                .eq(Job::getFlag, 0)
                .last("for update")
                .one();
        if (ObjectUtil.isNull(original)) {
            throw new BizException("工作记录不存在");
        }

        // 前端可能是「只改数量/只改日期」的局部提交，缺字段时先从库里补齐，
        // 否则 calculateJobSalary 里的 job.getQuantity() 会 NPE。
        job.setQuantity(ObjectUtil.defaultIfNull(job.getQuantity(), original.getQuantity()));
        job.setCategoryId(ObjectUtil.defaultIfNull(job.getCategoryId(), original.getCategoryId()));
        job.setModeId(ObjectUtil.defaultIfNull(job.getModeId(), original.getModeId()));
        // 工单归属不允许在编辑里改：能改的话就等于把一条普通记录挂到工单上，绕开认领校验
        job.setWorkOrderId(original.getWorkOrderId());

        // 认领记录的数量/方式变了，工单上的已认领计数要跟着调整，
        // 校验和扣减都在一条 UPDATE 里做（见 increaseClaimedQuantity）
        syncClaimedCounter(original, job);

        // 计算工作工资
        calculateJobSalary(job);

        // 更新工作记录
        if (!updateById(job)) {
            throw new BizException("修改工作记录失败：该记录不存在，或已被其他人修改，请刷新后重试");
        }
        return Result.ok();
    }

    /**
     * 认领记录被编辑时，同步工单上的已认领计数。
     *
     * <p>数量往上改要先原子加（剩余不够就整条失败）；往下改把计数减回去；
     * 换了工作方式则旧方式减掉、新方式加回，加不进去就抛异常，整个事务一起回滚。</p>
     *
     * @param original 编辑前的记录
     * @param updated  编辑后的记录
     */
    private void syncClaimedCounter(Job original, Job updated) {
        Integer workOrderId = original.getWorkOrderId();
        if (ObjectUtil.isNull(workOrderId)) {
            return;
        }

        int oldQuantity = original.getQuantity() == null ? 0 : original.getQuantity();
        int newQuantity = updated.getQuantity() == null ? 0 : updated.getQuantity();
        Integer oldMode = original.getModeId();
        Integer newMode = updated.getModeId();

        if (ObjectUtil.equal(oldMode, newMode)) {
            int delta = newQuantity - oldQuantity;
            if (delta > 0) {
                if (increaseClaimedQuantity(workOrderId, newMode, delta) == 0) {
                    throw new BizException(buildQuantityLimitMessage(workOrderId, newMode, oldQuantity));
                }
            } else if (delta < 0) {
                decreaseClaimedQuantity(workOrderId, newMode, -delta);
            }
            return;
        }

        decreaseClaimedQuantity(workOrderId, oldMode, oldQuantity);
        if (increaseClaimedQuantity(workOrderId, newMode, newQuantity) == 0) {
            throw new BizException(buildQuantityLimitMessage(workOrderId, newMode, 0));
        }
    }

    /**
     * 拼「这条最多只能填多少」的报错文案
     *
     * @param workOrderId 工单ID
     * @param modeId      工作方式
     * @param base        已经算在这条记录里的数量（换方式时为 0）
     * @return 报错文案
     */
    private String buildQuantityLimitMessage(Integer workOrderId, Integer modeId, int base) {
        JobWorkOrder workOrder = jobWorkOrderService.getById(workOrderId);
        int remain = ObjectUtil.isNull(workOrder) ? 0 : Math.max(remainingQuantity(workOrder, modeId), 0);
        return "改不了：该工单这个工种剩余可认领 " + remain + " 件，这条最多只能填 " + (base + remain) + " 件";
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

        // 锁住记录再删：删认领记录要把它占用的数量从工单计数里退回去，
        // 不锁的话两个删除请求可能各退一次
        Job job = lambdaQuery()
                .eq(Job::getId, jobId)
                .eq(Job::getFlag, 0)
                .last("for update")
                .one();
        if (ObjectUtil.isNull(job)) {
            throw new BizException("找不到对应的工作记录");
        }

        // 先退还工单上的已认领数量，再删记录；删失败会一起回滚
        decreaseClaimedQuantity(job.getWorkOrderId(), job.getModeId(), job.getQuantity());

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
