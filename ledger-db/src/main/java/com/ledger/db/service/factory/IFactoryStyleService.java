package com.ledger.db.service.factory;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ledger.common.result.Result;
import com.ledger.db.entity.FactoryStyle;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 成衣厂款式 服务类
 * </p>
 *
 * @author ahui
 * @since 2026-11-27
 */
public interface IFactoryStyleService extends IService<FactoryStyle> {

    /**
     * 分页查询成衣厂款式（带成衣厂名称与单价条数）
     *
     * @param factoryId   成衣厂ID
     * @param styleNumber 款式编号（模糊）
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @param flag        删除状态 0否 1是
     * @return result
     */
    Result<Object> queryFactoryStyleListByCondition(Integer factoryId, String styleNumber,
                                                    Integer currentPage, Integer pageSize, Integer flag);

    /**
     * 按成衣厂查款式下拉列表
     *
     * @param factoryId 成衣厂ID
     * @param flag      删除状态 0否 1是
     * @return result
     */
    Result<Object> queryStyleListByFactoryId(Integer factoryId, Integer flag);

    /**
     * 员工端按款式编号模糊查询样板图片
     *
     * @param styleNumber 款式编号
     * @param flag        删除状态 0否 1是
     * @return result
     */
    Result<Object> querySampleListByStyleNumber(String styleNumber, Integer flag);

    /**
     * 员工端样板图片列表：按成衣厂和款式编号筛选，只返回已经有图片的款式
     *
     * @param factoryId   成衣厂ID（可为空）
     * @param styleNumber 款式编号（模糊，可为空）
     * @param currentPage 当前页
     * @param pageSize    每页条数
     * @return result
     */
    Result<Object> querySampleStyleList(Integer factoryId, String styleNumber,
                                        Integer currentPage, Integer pageSize);

    /**
     * 新增成衣厂款式
     *
     * @param factoryStyle 款式实体
     * @return result
     */
    Result<Object> saveFactoryStyle(FactoryStyle factoryStyle);

    /**
     * 修改成衣厂款式
     *
     * @param factoryStyle 款式实体
     * @return result
     */
    Result<Object> updateFactoryStyle(FactoryStyle factoryStyle);

    /**
     * 删除成衣厂款式（有计件单价引用时拒删）
     *
     * @param id 款式ID
     * @return result
     */
    Result<Object> deleteFactoryStyle(Integer id);

    /**
     * 上传样板图片
     *
     * @param id   款式ID
     * @param file 图片文件
     * @return result，data 为新访问路径
     */
    Result<Object> uploadFactoryStyleImage(Integer id, MultipartFile file);

    /**
     * 删除样板图片
     *
     * @param id 款式ID
     * @return result
     */
    Result<Object> deleteFactoryStyleImage(Integer id);
}
