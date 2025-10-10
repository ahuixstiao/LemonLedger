package com.ledger.common.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: ahui
 * @Description: TODO 类型转换器
 * @DateTime: 2025/10/3 - 22:06
 **/
public class ConvertUtil {


    /**
     * Dto ,Vo ,Entity 相互转换
     * @param metaObject    元对象 --Dto，Vo，Entity
     * @param target      转换对象 --Dto，Vo，Entity
     * @return 新对象实例
     * @throws Exception
     */
    public static <T> T convertTarget(Object metaObject, Class<T> target) {
        if (ObjectUtil.isNull(metaObject)){
            return null;
        }
        // 获取将被转换类的实例
        T newInstance = null;
        try {
            // 创建实例
            newInstance = target.getDeclaredConstructor().newInstance();
            // 复制对象
            BeanUtils.copyProperties(metaObject, newInstance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newInstance;
    }

    /**
     * 深拷贝
     * @param metaObject    元对象 --Dto，Vo，Entity
     * @param target      转换对象 --Dto，Vo，Entity
     * @return 新对象实例
     * @throws Exception
     */
    public static <T> T deepCopy(Object metaObject, Class<T> target) {
        if (ObjectUtil.isNull(metaObject)){
            return null;
        }
        // 获取将被转换类的实例
        return Convert.convert(target, metaObject);
    }

    /**
     * 转换List集合对象
     * @param metaObjectList 元集合
     * @param target 将要转换的目标类
     * @return List<T> 新对象集合
     */
    public static <T> List<T> convertTargetList(List<?> metaObjectList, Class<T> target) {
        List<T> targetList = new ArrayList<>();
        for (Object source : metaObjectList) {
            targetList.add(deepCopy(source, target));
        }
        return targetList;
    }

    /**
     * Page<--Dto，Vo，Entity> 分页对象转 Page<--Dto，Vo，Entity>
     * @param metaPage 元分页对象
     * @param target 要转换的目标类 --Dto，Vo，Entity
     * @return newPage<Vo>
     */
    public static <T, E> IPage<E> convertPage(IPage<T> metaPage, Class<E> target) {
        IPage<E> convert = metaPage.convert(item -> {
            try {
                // 获取将被转换类的实例
                E newVo = convertTarget(item, target);
                return newVo;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return convert;
    }

    /**
     * 字符串转List集合
     * @param targetString 将要转换的字符串
     * @return List<String>
     */
    public static List<String> toStringList(String targetString) {
        var replace = targetString
                .replace("[", "")
                .replace("]", "");
        // 格式化字符串为List集合
        return Arrays.asList(replace);
    }

}
