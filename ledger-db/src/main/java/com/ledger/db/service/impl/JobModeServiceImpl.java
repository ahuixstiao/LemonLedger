package com.ledger.db.service.impl;

import com.ledger.db.entity.JobMode;
import com.ledger.db.mapper.JobModeMapper;
import com.ledger.db.service.IJobModeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
@RequiredArgsConstructor(onConstructor_ =  @Autowired)
public class JobModeServiceImpl extends ServiceImpl<JobModeMapper, JobMode> implements IJobModeService {

}
