package com.ledger.api;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.ledger.common.entity.ParentEntity;
import com.ledger.db.entity.dto.JobDTO;
import com.ledger.db.mapper.JobMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.http.HttpClient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SpringBootTest
@Component
class LedgerApplicationTests {

    @Value("${db.url}")
    private String url;

    @Value("${db.username}")
    private String username;

    @Value("${db.password}")
    private String password;

    @Autowired
    private JobMapper jobMapper;

    @Test
    void contextLoads() {
        // 逆向工程
        //codeGenerator("factory_quotation");

        //System.out.println(LocalDateTime.parse("2025-10-05T08:21:26.663105").format(DateTimeFormatter.ISO_DATE));
        //System.out.println(LocalDate.parse("2025-10-01").format(DateTimeFormatter.ISO_DATE));

        //根据员工ID查询员工工作信息
        /*Page<JobDTO> jobDTOList = jobMapper.selectJobListByEmployeeId(
                new Page<>(1, 5),0,
               "2025-10-01 T08:21:26",
                "2025-10-15", 0);
        jobDTOList.getRecords().forEach(System.out::println);*/


        // 查询所有员工当天的工作信息
        /*Page<JobDTO> page = jobMapper.selectJobListByDefaultCurrentDay(
                new Page<>(1, 5),0);
        page.getRecords().forEach(System.out::println);*/


        // 查询员工工资
        /*JobDTO jobDTO = jobMapper.calculateSalaryByEmployeeIdAndDate(
                1,
                "", "2025-09-01",
                0);
        System.out.println(jobDTO);*/

    }

    /**
     * 选择表进行逆向工程
     *
     * @param tableName 表名  如需全表生成则输入all 多表则用逗号分割
     */
    public void codeGenerator(String tableName) {
        FastAutoGenerator.create(url, username, password)
                // 全局配置
                .globalConfig(builder -> builder
                        // 设置作者
                        .author("ahui")
                        // 日期类型
                        .dateType(DateType.TIME_PACK)
                        // 指定生成文件的路径
                        .outputDir("../ledger-db/src/main/java/")
                        // 禁止生成后打开对应目录
                        .disableOpenDir()
                )
                // 包配置
                .packageConfig(builder -> builder
                        // 设置父包名
                        .parent("com.ledger.db")
                        // 设置.xml文件生成路径
                        .pathInfo(
                                Collections.singletonMap(
                                        OutputFile.xml, "../ledger-db/src/main/resources/mapper/"
                                )
                        )
                )
                // 策略配置
                .strategyConfig(builder -> builder
                        // 构建表配置
                        .addInclude(getTables(tableName)) // 需要逆向工程的表名 如全表生成则输入all

                        // 构建entity配置
                        .entityBuilder()
                        .enableLombok() // 实体类启用lombok
                        .logicDeleteColumnName("flag") // 逻辑删除字段名称
                        .logicDeletePropertyName("Integer") // 逻辑删除字段属性名
                        .naming(NamingStrategy.underline_to_camel) // 表映射实体命名策略 驼峰命名
                        .columnNaming(NamingStrategy.underline_to_camel) // 表字段映射实体命名策略 驼峰命名
                        .superClass(ParentEntity.class) // 继承父类
                        .addSuperEntityColumns( // 添加父类公共字段
                                "id",
                                "created_by", "created_time",
                                "updated_by", "updated_time",
                                "flag", "remark"
                        )

                        // 构建controller配置
                        .controllerBuilder()
                        .enableRestStyle() // 生成Rest风格的Controller
                        .enableHyphenStyle() // 驼峰转连字符 _

                        // 构建mapper接口配置
                        .mapperBuilder()
                        .enableMapperAnnotation() // 使用@Mapper注解

                        // 构建完成
                        .build()
                )
                // 模板引擎
                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                // 执行
                .execute();
    }

    // 处理 all 情况
    private static List<String> getTables(String tables) {
        return "all".equals(tables) ? Collections.emptyList() : Arrays.asList(tables.split(","));
    }


}
