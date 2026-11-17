-- =============================================================
-- LemonLedger 0.0.5 数据迁移：工单认领计数（原子扣减）
-- -------------------------------------------------------------
-- 做了什么：
--   1. job_work_order 加两列 claimed_emboss / claimed_glue，
--      分别记录「压花(mode_id=1)」「刮胶(mode_id=2)」已经认领了多少；
--   2. 用 job 里的认领记录回填这两列，让存量数据和计数器对得上。
--
-- 为什么：
--   认领接口原来是「先 SUM 已认领量、再插入新记录」，并发下两个请求可能
--   同时读到同一个剩余量，双双通过校验 → 同一张工单被超领。改成
--   「UPDATE ... SET claimed_x = claimed_x + n WHERE 剩余 >= n」之后，
--   判断和扣减在同一条 SQL 里，由 InnoDB 的行锁保证不会同时通过。
--
-- 没有删任何数据，也没有改任何金额。
-- 运行前请先备份（deploy/backup-mysql.ps1），在 MySQL 8 上执行。
--
-- ⚠ 本脚本只能跑一次。重复执行会因为列已存在而报错，那是正常的。
-- ⚠ 先跑第 2 步的校验查询，确认返回 0 行，再启动新版后端。
-- =============================================================

-- ---------- 1. 加计数列 ----------
ALTER TABLE `job_work_order`
    ADD COLUMN `claimed_emboss` int NOT NULL DEFAULT '0' COMMENT '已认领数量（压花，mode_id=1）' AFTER `quantity`,
    ADD COLUMN `claimed_glue` int NOT NULL DEFAULT '0' COMMENT '已认领数量（刮胶，mode_id=2）' AFTER `claimed_emboss`;

-- ---------- 2. 从认领记录回填 ----------
-- 只统计 flag=0 的认领记录；历史上有超领的工单，这里如实回填成大于 quantity 的值，
-- 不抹平——剩余量在查询时用 greatest(quantity - 已完成, 0) 兜住。
UPDATE `job_work_order` w
LEFT JOIN (
    SELECT work_order_id, SUM(quantity) AS claimed
    FROM job
    WHERE flag = 0 AND work_order_id IS NOT NULL AND mode_id = 1
    GROUP BY work_order_id
) e ON e.work_order_id = w.id
LEFT JOIN (
    SELECT work_order_id, SUM(quantity) AS claimed
    FROM job
    WHERE flag = 0 AND work_order_id IS NOT NULL AND mode_id = 2
    GROUP BY work_order_id
) g ON g.work_order_id = w.id
SET w.claimed_emboss = COALESCE(e.claimed, 0),
    w.claimed_glue   = COALESCE(g.claimed, 0);

-- ---------- 3. 校验 ----------
-- 3.1 计数器与明细不一致的工单（应该返回 0 行）
-- SELECT w.id, w.quantity, w.claimed_emboss, w.claimed_glue,
--        COALESCE(e.claimed, 0) AS real_emboss,
--        COALESCE(g.claimed, 0) AS real_glue
-- FROM job_work_order w
-- LEFT JOIN (SELECT work_order_id, SUM(quantity) claimed FROM job
--            WHERE flag = 0 AND work_order_id IS NOT NULL AND mode_id = 1 GROUP BY work_order_id) e
--        ON e.work_order_id = w.id
-- LEFT JOIN (SELECT work_order_id, SUM(quantity) claimed FROM job
--            WHERE flag = 0 AND work_order_id IS NOT NULL AND mode_id = 2 GROUP BY work_order_id) g
--        ON g.work_order_id = w.id
-- WHERE w.flag = 0
--   AND (w.claimed_emboss <> COALESCE(e.claimed, 0) OR w.claimed_glue <> COALESCE(g.claimed, 0));

-- 3.2 历史上超领的工单（超过 0 行是正常的，说明这些单以前就领多了，不会被自动改掉）
-- SELECT w.id, w.number, w.quantity, w.claimed_emboss, w.claimed_glue
-- FROM job_work_order w
-- WHERE w.flag = 0 AND (w.claimed_emboss > w.quantity OR w.claimed_glue > w.quantity);
