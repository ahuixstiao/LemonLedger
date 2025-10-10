package com.ledger.db.service.impl;

import com.ledger.db.entity.JobCategory;
import com.ledger.db.mapper.JobCategoryMapper;
import com.ledger.db.service.IJobCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 工作类型表 服务实现类
 * </p>
 *
 * @author ahui
 * @since 2025-10-02
 */
@Service
@RequiredArgsConstructor(onConstructor_ =  @Autowired)
public class JobCategoryServiceImpl extends ServiceImpl<JobCategoryMapper, JobCategory> implements IJobCategoryService {

}
