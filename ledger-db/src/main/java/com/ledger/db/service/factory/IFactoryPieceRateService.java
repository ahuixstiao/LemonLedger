package com.ledger.db.service.factory;

import com.ledger.common.result.Result;
import com.ledger.db.entity.FactoryPieceRate;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 成衣厂计件单价表 服务类
 * </p>
 *
 * @author ahui
 * @since 2025-10-16
 */
public interface IFactoryPieceRateService extends IService<FactoryPieceRate> {

    /**
     * 查询成衣厂计件单价列表 如果有条件则按条件查询
     *
     * @param factoryId   工厂ID
     * @param styleNumber 款式编号
     * @param categoryId  工作类型ID
     * @param currentPage 当前页
     * @param pageSize    页面条数
     * @param flag        状态
     * @return result
     */
    Result<Object> queryFactoryPieceRateListByCondition(
            Integer factoryId, String styleNumber, Integer categoryId,
            Integer currentPage, Integer pageSize, Integer flag);

    /**
     * 按工厂ID查询对应工厂的计件单价单款式编号列表
     *
     * @param factoryId 工厂ID
     * @param flag      状态
     * @return result
     */
    Result<Object> queryStyleNumberListByFactoryId(Integer factoryId, Integer flag);

    /**
     * 员工端按款式编号查询样板列表（仅包含款式编号和图片路径）。
     *
     * @param styleNumber 款式编号（支持模糊匹配）
     * @param flag        删除状态 0否 1是
     * @return result
     */
    Result<Object> querySampleListByStyleNumber(String styleNumber, Integer flag);

    /**
     * 保存成衣厂计件单价信息
     *
     * @param factoryPieceRate 成衣厂计件单价实体
     * @return result
     */
    Result<Object> saveFactoryPieceRateInfo(FactoryPieceRate factoryPieceRate);

    /**
     * 更新成衣厂计件单价信息
     *
     * @param factoryPieceRate 成衣厂计件单价实体
     * @return result
     */
    Result<Object> updateFactoryPieceRateInfo(FactoryPieceRate factoryPieceRate);

    /**
     * 上传成衣厂计件单价图片到本地外部目录。
     *
     * @param id   成衣厂计件单价ID
     * @param file 图片文件
     * @return result
     */
    Result<Object> uploadFactoryPieceRateImage(Integer id, MultipartFile file);

    /**
     * 删除成衣厂计件单价图片（删除磁盘文件并清空图片路径）。
     *
     * @param id 成衣厂计件单价ID
     * @return result
     */
    Result<Object> deleteFactoryPieceRateImage(Integer id);

    /**
     * 删除成衣厂计件单价信息
     *
     * @param id 成衣厂计件单价ID
     * @return result
     */
    Result<Object> deleteFactoryPieceRateById(Integer id);

}
