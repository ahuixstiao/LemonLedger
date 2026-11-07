-- ============================================================
-- LemonLedger 0.0.2 数据库升级脚本
-- 适用于：已经在用旧版（唯一键不带 flag）的数据库
-- 新装数据库不需要执行本脚本，直接导入 ledger.sql 即可
--
-- 执行方式（在宿主机上，按你实际的账号密码改）：
--   mysql -u root -p ledger < deploy/migrate-0.0.2.sql
-- 或者在 Navicat / DataGrip 里连上 ledger 库，整段执行。
--
-- 执行前请先备份：deploy/backup-mysql.ps1
-- ============================================================

-- ------------------------------------------------------------
-- 1. 员工表：唯一键 unique_name(name) -> unique_name(name, flag)
-- ------------------------------------------------------------
-- 逻辑删除只把 flag 置 1，不删行。唯一键只看 name 时，
-- 删掉"张三"后数据库仍认为这个名字被占用，同名员工再也加不进来。
ALTER TABLE `employee`
    DROP INDEX `unique_name`,
    ADD UNIQUE KEY `unique_name` (`name`, `flag`);

-- ------------------------------------------------------------
-- 2. 成衣厂表：唯一键 unique_factory_name(factory_name) -> (factory_name, flag)
-- ------------------------------------------------------------
ALTER TABLE `factory`
    DROP INDEX `unique_factory_name`,
    ADD UNIQUE KEY `unique_factory_name` (`factory_name`, `flag`);

-- ------------------------------------------------------------
-- 3.（可选）账单金额列放宽
-- 当前是 decimal(8,2)，上限 999999.99。单床账单一般到不了，
-- 但如果哪天出现超过 100 万的单床金额会直接插入失败。
-- 想放宽就取消下面的注释（把类型改大不会丢数据）：
-- ------------------------------------------------------------
-- ALTER TABLE `factory_bill`
--     MODIFY COLUMN `bill` decimal(12,2) DEFAULT NULL COMMENT '本床账单';

-- ------------------------------------------------------------
-- 4. 验证
-- ------------------------------------------------------------
-- SHOW INDEX FROM `employee` WHERE Key_name = 'unique_name';
-- SHOW INDEX FROM `factory`  WHERE Key_name = 'unique_factory_name';
