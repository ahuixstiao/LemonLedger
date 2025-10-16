package com.ledger.db.impl.job;

import com.ledger.db.entity.JobQuotation;
import com.ledger.db.mapper.JobQuotationMapper;
import com.ledger.db.service.job.IJobQuotationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 工作报价表 服务实现类
 * </p>
 *
 * @author ahui
 * @since 2025-10-16
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class JobQuotationServiceImpl extends ServiceImpl<JobQuotationMapper, JobQuotation> implements IJobQuotationService {

}
