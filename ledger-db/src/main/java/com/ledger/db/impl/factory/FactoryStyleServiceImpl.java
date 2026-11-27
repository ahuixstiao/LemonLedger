package com.ledger.db.impl.factory;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.common.exception.BizException;
import com.ledger.common.result.Result;
import com.ledger.db.entity.FactoryPieceRate;
import com.ledger.db.entity.FactoryStyle;
import com.ledger.db.entity.dto.FactoryStyleDto;
import com.ledger.db.impl.LedgerServiceImpl;
import com.ledger.db.mapper.FactoryPieceRateMapper;
import com.ledger.db.mapper.FactoryStyleMapper;
import com.ledger.db.service.factory.IFactoryStyleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 成衣厂款式 服务实现类
 * </p>
 *
 * @author ahui
 * @since 2026-11-27
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FactoryStyleServiceImpl extends LedgerServiceImpl<FactoryStyleMapper, FactoryStyle> implements IFactoryStyleService {

    private static final String IMAGE_SUB_DIR = "factoryStyle";

    private final FactoryStyleMapper factoryStyleMapper;

    private final FactoryPieceRateMapper factoryPieceRateMapper;

    private final ImageStorage imageStorage;

    /**
     * 分页查询成衣厂款式
     *
     * @param factoryId   成衣厂ID
     * @param styleNumber 款式编号（模糊）
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @param flag        删除状态 0否 1是
     * @return result
     */
    @Override
    @Transactional(readOnly = true)
    public Result<Object> queryFactoryStyleListByCondition(Integer factoryId, String styleNumber,
                                                           Integer currentPage, Integer pageSize, Integer flag) {
        Page<FactoryStyleDto> page = new Page<>(
                currentPage == null || currentPage < 1 ? 1 : currentPage,
                pageSize == null || pageSize < 1 ? 10 : pageSize);
        return Result.ok(factoryStyleMapper.selectFactoryStyleListByCondition(page, factoryId, styleNumber, flag));
    }

    /**
     * 按成衣厂查款式下拉列表
     *
     * @param factoryId 成衣厂ID
     * @param flag      删除状态 0否 1是
     * @return result
     */
    @Override
    @Transactional(readOnly = true)
    public Result<Object> queryStyleListByFactoryId(Integer factoryId, Integer flag) {
        return Result.ok(factoryStyleMapper.selectStyleListByFactoryId(factoryId, flag));
    }

    /**
     * 员工端按款式编号模糊查询样板图片
     *
     * @param styleNumber 款式编号
     * @param flag        删除状态 0否 1是
     * @return result
     */
    @Override
    @Transactional(readOnly = true)
    public Result<Object> querySampleListByStyleNumber(String styleNumber, Integer flag) {
        return Result.ok(factoryStyleMapper.selectSampleListByStyleNumber(styleNumber, flag));
    }

    /**
     * 新增成衣厂款式：同一成衣厂下款式编号不能重复
     *
     * @param factoryStyle 款式实体
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> saveFactoryStyle(FactoryStyle factoryStyle) {
        if (ObjectUtil.isEmpty(factoryStyle.getFactoryId())) {
            throw new BizException("请先选择成衣厂");
        }
        if (StrUtil.isBlank(factoryStyle.getStyleNumber())) {
            throw new BizException("请填写款式编号");
        }
        factoryStyle.setStyleNumber(factoryStyle.getStyleNumber().trim());
        checkStyleNumberUnique(factoryStyle.getFactoryId(), factoryStyle.getStyleNumber(), null);
        // 图片只能走上传接口，这里不接受前端直接写路径
        factoryStyle.setImagePath(null);
        if (!save(factoryStyle)) {
            throw new BizException("新增款式失败，请重试");
        }
        return Result.ok();
    }

    /**
     * 修改成衣厂款式：编号不能和同厂其它款式重复
     *
     * @param factoryStyle 款式实体
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> updateFactoryStyle(FactoryStyle factoryStyle) {
        if (ObjectUtil.isEmpty(factoryStyle.getId())) {
            throw new BizException("款式ID不能为空");
        }
        FactoryStyle original = getById(factoryStyle.getId());
        if (ObjectUtil.isEmpty(original)) {
            throw new BizException("款式不存在，请刷新后重试");
        }
        String styleNumber = StrUtil.isBlank(factoryStyle.getStyleNumber())
                ? original.getStyleNumber()
                : factoryStyle.getStyleNumber().trim();
        Integer factoryId = ObjectUtil.isEmpty(factoryStyle.getFactoryId())
                ? original.getFactoryId()
                : factoryStyle.getFactoryId();
        checkStyleNumberUnique(factoryId, styleNumber, original.getId());

        factoryStyle.setFactoryId(factoryId);
        factoryStyle.setStyleNumber(styleNumber);
        // 图片改用上传/删除接口维护，这里不覆盖
        factoryStyle.setImagePath(null);
        if (!updateById(factoryStyle)) {
            throw new BizException("修改款式失败：款式不存在，或已被其他人修改，请刷新后重试");
        }
        return Result.ok();
    }

    /**
     * 删除成衣厂款式：还被计件单价引用时拒绝删除
     *
     * @param id 款式ID
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> deleteFactoryStyle(Integer id) {
        if (ObjectUtil.isEmpty(id)) {
            throw new BizException("款式ID不能为空");
        }
        long used = factoryPieceRateMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FactoryPieceRate>()
                .eq(FactoryPieceRate::getStyleId, id)
                .eq(FactoryPieceRate::getFlag, 0));
        if (used > 0) {
            throw new BizException("该款式下还有 " + used + " 条计件单价，先删掉单价再删款式");
        }
        FactoryStyle style = getById(id);
        if (ObjectUtil.isNotEmpty(style) && StrUtil.isNotBlank(style.getImagePath())) {
            imageStorage.delete(style.getImagePath());
        }
        if (!removeById(id)) {
            throw new BizException("删除款式失败：款式不存在或已被删除");
        }
        return Result.ok();
    }

    /**
     * 上传样板图片，成功返回新的访问路径
     *
     * @param id   款式ID
     * @param file 图片文件
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> uploadFactoryStyleImage(Integer id, MultipartFile file) {
        FactoryStyle style = getById(id);
        if (ObjectUtil.isEmpty(style)) {
            throw new BizException("款式不存在");
        }
        String accessPath = imageStorage.save(file, IMAGE_SUB_DIR, id);
        if (!lambdaUpdate().eq(FactoryStyle::getId, id).set(FactoryStyle::getImagePath, accessPath).update()) {
            imageStorage.delete(accessPath);
            throw new BizException("图片已写入磁盘，但保存图片路径失败，请重试");
        }
        return Result.ok(accessPath);
    }

    /**
     * 删除样板图片（磁盘文件一起删）
     *
     * @param id 款式ID
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> deleteFactoryStyleImage(Integer id) {
        FactoryStyle style = getById(id);
        if (ObjectUtil.isEmpty(style)) {
            throw new BizException("款式不存在");
        }
        if (StrUtil.isBlank(style.getImagePath())) {
            throw new BizException("该款式暂无样板图片");
        }
        imageStorage.delete(style.getImagePath());
        if (!lambdaUpdate().eq(FactoryStyle::getId, id).set(FactoryStyle::getImagePath, null).update()) {
            throw new BizException("图片文件已删除，但清空数据库中的图片路径失败，请重试");
        }
        return Result.ok("删除图片成功", null);
    }

    /**
     * 校验同一成衣厂下的款式编号是否唯一
     *
     * @param factoryId   成衣厂ID
     * @param styleNumber 款式编号
     * @param excludeId   修改时排除自身
     */
    private void checkStyleNumberUnique(Integer factoryId, String styleNumber, Integer excludeId) {
        boolean exists = lambdaQuery()
                .eq(FactoryStyle::getFactoryId, factoryId)
                .eq(FactoryStyle::getStyleNumber, styleNumber)
                .eq(FactoryStyle::getFlag, 0)
                .ne(ObjectUtil.isNotEmpty(excludeId), FactoryStyle::getId, excludeId)
                .exists();
        if (exists) {
            throw new BizException("这个成衣厂已经有款式「" + styleNumber + "」了");
        }
    }
}
