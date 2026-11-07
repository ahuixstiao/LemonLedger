-- ============================================================
-- LemonLedger 0.0.3 数据库升级脚本
-- 适用于：已经在用旧版数据库、里面有真实账目的情况
-- 新装数据库不需要执行本脚本，直接导入 ledger.sql 即可
--
-- 执行方式（在宿主机上，按你实际的账号密码改）：
--   mysql -u root -p ledger < deploy/migrate-0.0.3.sql
-- 或者在 Navicat / DataGrip 里连上 ledger 库，整段执行。
--
-- 执行前请先备份：deploy/backup-mysql.ps1
-- 本脚本只加索引、放宽一列的约束，不删不改进数据，可以反复执行前先看一下
-- 下面第 1 步的说明（那一列改成可空不会丢数据）。
-- ============================================================

-- ------------------------------------------------------------
-- 1. 员工表：mode_id 允许为空
-- ------------------------------------------------------------
-- 原来 mode_id NOT NULL 且唯一键只有 (name, flag)：一个人只能挂一种工作方式，
-- 压花、刮胶都会做的人就没法建档。改成可空之后，档案上可以不填默认方式，
-- 认领工单时再选本次按哪种方式算钱（前端会把 modeId 一起提交）。
-- 这一步不修改任何已有值，只是允许写 NULL。
ALTER TABLE `employee`
    MODIFY COLUMN `mode_id` int DEFAULT NULL COMMENT '默认工作模式 1压花 2刮胶，可为空';

-- ------------------------------------------------------------
-- 2. 账单表：补索引
-- ------------------------------------------------------------
-- 账单的列表、统计、导出默认都是「某成衣厂 + 某段日期」，
-- 原来只有 number / quotation_id 两个索引，查询基本等于全表扫。
ALTER TABLE `factory_bill`
    ADD INDEX `factory_bill_factory_date_index` (`factory_id`, `created_date`),
    ADD INDEX `factory_bill_date_index` (`created_date`);

-- ------------------------------------------------------------
-- 3. 工作记录表：补索引
-- ------------------------------------------------------------
-- (1) 认领时按「工单 + 工作方式」算已认领数量，走这条 SQL SUM：
--     SELECT COALESCE(SUM(quantity), 0) FROM job
--     WHERE work_order_id = ? AND mode_id = ? AND flag = 0
-- (2) 工资统计是「某员工 + 某段日期」，两列做成联合索引。
ALTER TABLE `job`
    ADD INDEX `job_work_order_mode_index` (`work_order_id`, `mode_id`, `flag`),
    ADD INDEX `job_employee_date_index` (`employee_id`, `created_date`);

-- ------------------------------------------------------------
-- 4. 工单表：补索引
-- ------------------------------------------------------------
-- 工单列表默认按日期取，没有索引会全表扫 + 额外排序。
ALTER TABLE `job_work_order`
    ADD INDEX `job_work_order_date_index` (`created_date`),
    ADD INDEX `job_work_order_factory_date_index` (`factory_id`, `created_date`);

-- ------------------------------------------------------------
-- 5. 各表补 updated_date（最后修改日期）
-- ------------------------------------------------------------
-- 原来只有 created_date：同一天里改了哪条、改过几次，事后完全看不出来。
-- 现在每次新增/修改都会由 MyBatis-Plus 自动填这一列（业务代码不用改）。
-- 已有数据的 updated_date 先是 NULL，之后再改一次就会填上。
-- 注意：本脚本跑在 0.0.4 改名之前，所以下面用的还是**旧表名**
-- （factory_quotation / job_quotation）。0.0.4 第 0 步会连列一起改名。
ALTER TABLE `employee`             ADD COLUMN `updated_date` date DEFAULT NULL COMMENT '最后修改日期' AFTER `created_date`;
ALTER TABLE `factory`              ADD COLUMN `updated_date` date DEFAULT NULL COMMENT '最后修改日期' AFTER `created_date`;
ALTER TABLE `factory_bill`         ADD COLUMN `updated_date` date DEFAULT NULL COMMENT '最后修改日期' AFTER `created_date`;
ALTER TABLE `factory_job_category` ADD COLUMN `updated_date` date DEFAULT NULL COMMENT '最后修改日期' AFTER `created_date`;
ALTER TABLE `factory_quotation`    ADD COLUMN `updated_date` date DEFAULT NULL COMMENT '最后修改日期' AFTER `created_date`;
ALTER TABLE `job`                  ADD COLUMN `updated_date` date DEFAULT NULL COMMENT '最后修改日期' AFTER `created_date`;
ALTER TABLE `job_category`         ADD COLUMN `updated_date` date DEFAULT NULL COMMENT '最后修改日期' AFTER `created_date`;
ALTER TABLE `job_mode`             ADD COLUMN `updated_date` date DEFAULT NULL COMMENT '最后修改日期' AFTER `created_date`;
ALTER TABLE `job_quotation`        ADD COLUMN `updated_date` date DEFAULT NULL COMMENT '最后修改日期' AFTER `created_date`;
ALTER TABLE `job_work_order`       ADD COLUMN `updated_date` date DEFAULT NULL COMMENT '最后修改日期' AFTER `created_date`;

-- ------------------------------------------------------------
-- 6. 验证
-- ------------------------------------------------------------
-- SHOW INDEX FROM `factory_bill` WHERE Key_name LIKE '%date%';
-- SHOW INDEX FROM `job` WHERE Key_name IN ('job_work_order_mode_index', 'job_employee_date_index');
-- SHOW INDEX FROM `job_work_order` WHERE Key_name LIKE '%date%';
-- SHOW COLUMNS FROM `employee` LIKE 'mode_id';   -- Null 列应为 YES
-- SHOW COLUMNS FROM `job` LIKE 'updated_date';   -- 十张表都应有这一列
