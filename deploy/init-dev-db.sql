-- ============================================================
-- 本地开发库（可选）
-- 目的：让本地开发不要直接改真实账目库 ledger。
--
-- 用法：
--   1. 执行本脚本创建 ledger_dev
--   2. 把 ledger 的结构和数据复制过去（二选一）：
--        mysqldump -u root -p ledger | mysql -u root -p ledger_dev
--      或者用 Navicat 的"数据传输"，源 ledger，目标 ledger_dev
--   3. 把 ledger-db/src/main/resources/application-db-dev.yml 里
--      4 处 url 的 /ledger 改成 /ledger_dev
--   4. 启动时加环境变量：SPRING_PROFILES_ACTIVE=db-dev,common
-- ============================================================

CREATE DATABASE IF NOT EXISTS `ledger_dev`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

-- 复制完成后，可以在 ledger_dev 里随便改数据，不影响真实账目。
