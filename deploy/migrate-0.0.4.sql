-- =============================================================
-- LemonLedger 0.0.4 数据迁移：新增「床」表（batch）
-- -------------------------------------------------------------
-- 做了什么：
--   1. 建 batch 表（厂 + 床号 + 款式 一条）；
--   2. factory_bill / job / job_work_order 各加 batch_id 列和索引；
--   3. 用现有单据里的 (厂, 床号, 款式) 反推出一张张床，回填 batch_id。
--
-- 没有删任何数据，也没有改任何金额。
-- 运行前请先备份（deploy/backup-mysql.ps1），在 MySQL 8 上执行。
--
-- ⚠ 顺序很重要：先做第 0 步（表/列改名 + 字典合并），后面几步才认得新表名。
-- ⚠ 本脚本只能跑一次。第 0 步的表名/列名改名重复执行会报「表不存在 / 列不存在」，
--    那是正常的，说明它已经跑过了，别慌。
-- =============================================================

-- ---------- 0. 表名 / 列名改名（报价 → 计件单价） ----------
-- 只改名字，不动数据。MySQL 8 的 RENAME 是原子的。
RENAME TABLE `factory_quotation` TO `factory_piece_rate`,
             `job_quotation`     TO `job_piece_rate`;

ALTER TABLE `factory_piece_rate` RENAME COLUMN `quotation` TO `price`;
ALTER TABLE `job_piece_rate`     RENAME COLUMN `quotation` TO `price`;
ALTER TABLE `factory_bill`       RENAME COLUMN `quotation_id` TO `piece_rate_id`;

-- 索引名还是旧的（factory_bill_quotation_id_index 之类），不影响功能，想整齐可以顺手改：
-- ALTER TABLE `factory_bill` RENAME INDEX `factory_bill_quotation_id_index` TO `factory_bill_piece_rate_index`;

-- ---------- 0.5 工作类型字典合并（factory_job_category → job_category） ----------
-- 两套字典原来各存一份，现在只留 job_category。
-- 旧表不删，改名成 _bak 留作对照，确认没问题后你自己再决定删不删。

-- 0.5.1 先把旧字典整表留个底
RENAME TABLE `factory_job_category` TO `factory_job_category_bak`;

-- 0.5.2 旧字典里有、新字典里没有的名字，补进去
INSERT INTO `job_category` (`category`, `version`, `created_date`, `updated_date`, `flag`, `remark`)
SELECT f.`category`, 0, CURDATE(), CURDATE(), 0, '由原 factory_job_category 合并而来'
FROM `factory_job_category_bak` f
WHERE f.`flag` = 0
  AND NOT EXISTS (
    SELECT 1 FROM `job_category` j
    WHERE j.`category` = f.`category` AND j.`flag` = 0
  );

-- 0.5.3 成衣厂计件单价上挂的还是旧字典 id，按「名字」重新对到新字典 id
UPDATE `factory_piece_rate` p
JOIN `factory_job_category_bak` f ON f.`id` = p.`category_id`
JOIN `job_category` j ON j.`category` = f.`category` AND j.`flag` = 0
SET p.`category_id` = j.`id`;

-- 0.5.4 验证：这条查询应该返回 0 行（返回的行说明该单价的工作类型对不上新字典）
-- SELECT p.`id`, p.`style_number`, p.`category_id` FROM factory_piece_rate p
-- LEFT JOIN job_category j ON j.`id` = p.`category_id` AND j.`flag` = 0
-- WHERE p.`flag` = 0 AND j.`id` IS NULL;


-- ---------- 1. 建床表 ----------
CREATE TABLE IF NOT EXISTS `batch` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `factory_id` int NOT NULL COMMENT '成衣厂ID',
  `number` varchar(32) NOT NULL COMMENT '床号',
  `style_number` varchar(32) NOT NULL DEFAULT '' COMMENT '款式编号',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `created_date` date NOT NULL COMMENT '创建时间',
  `updated_date` date DEFAULT NULL COMMENT '最后修改日期',
  `flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除状态 0否 1是',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_batch` (`factory_id`, `number`, `style_number`, `flag`),
  KEY `batch_number_index` (`number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='床表';

-- ---------- 2. 三张单据加 batch_id ----------
ALTER TABLE `factory_bill`
  ADD COLUMN `batch_id` int DEFAULT NULL COMMENT '床ID，关联 batch.id' AFTER `number`,
  ADD KEY `factory_bill_batch_index` (`batch_id`);

ALTER TABLE `job`
  ADD COLUMN `batch_id` int DEFAULT NULL COMMENT '床ID，关联 batch.id' AFTER `work_order_id`,
  ADD KEY `job_batch_index` (`batch_id`);

ALTER TABLE `job_work_order`
  ADD COLUMN `batch_id` int DEFAULT NULL COMMENT '床ID，关联 batch.id' AFTER `quantity`,
  ADD KEY `job_work_order_batch_index` (`batch_id`);

-- ---------- 3. 从工单反推床 ----------
INSERT INTO `batch` (`factory_id`, `number`, `style_number`, `version`, `created_date`, `flag`)
SELECT DISTINCT `factory_id`, `number`, IFNULL(`style_number`, ''), 0, CURDATE(), 0
FROM `job_work_order`
WHERE `flag` = 0
  AND NOT EXISTS (
    SELECT 1 FROM `batch` b
    WHERE b.`factory_id` = `job_work_order`.`factory_id`
      AND b.`number` = `job_work_order`.`number`
      AND b.`style_number` = IFNULL(`job_work_order`.`style_number`, '')
      AND b.`flag` = 0
  );

-- ---------- 4. 从工作记录反推床（有些手工补录没有工单） ----------
INSERT INTO `batch` (`factory_id`, `number`, `style_number`, `version`, `created_date`, `flag`)
SELECT DISTINCT `factory_id`, `number`, IFNULL(`style_number`, ''), 0, CURDATE(), 0
FROM `job`
WHERE `flag` = 0
  AND NOT EXISTS (
    SELECT 1 FROM `batch` b
    WHERE b.`factory_id` = `job`.`factory_id`
      AND b.`number` = `job`.`number`
      AND b.`style_number` = IFNULL(`job`.`style_number`, '')
      AND b.`flag` = 0
  );

-- ---------- 5. 从账单反推床（账单本身没有款式，款式取计件单价上的） ----------
INSERT INTO `batch` (`factory_id`, `number`, `style_number`, `version`, `created_date`, `flag`)
SELECT DISTINCT fb.`factory_id`, fb.`number`, IFNULL(fpr.`style_number`, ''), 0, CURDATE(), 0
FROM `factory_bill` fb
JOIN `factory_piece_rate` fpr ON fpr.`id` = fb.`piece_rate_id`
WHERE fb.`flag` = 0
  AND NOT EXISTS (
    SELECT 1 FROM `batch` b
    WHERE b.`factory_id` = fb.`factory_id`
      AND b.`number` = fb.`number`
      AND b.`style_number` = IFNULL(fpr.`style_number`, '')
      AND b.`flag` = 0
  );

-- ---------- 5.5 账单没挂计件单价的（手工补录，piece_rate_id 为空） ----------
-- 这类账单不知道款式，按「厂 + 床号 + 空款式」单独建一张床，别让它们悬空。
-- （第一次真实迁移时就是漏了这步，13 条账单没挂上床，补在这里）
INSERT INTO `batch` (`factory_id`, `number`, `style_number`, `version`, `created_date`, `flag`)
SELECT DISTINCT fb.`factory_id`, fb.`number`, '', 0, CURDATE(), 0
FROM `factory_bill` fb
WHERE fb.`flag` = 0
  AND fb.`piece_rate_id` IS NULL
  AND fb.`batch_id` IS NULL
  AND NOT EXISTS (
    SELECT 1 FROM `batch` b
    WHERE b.`factory_id` = fb.`factory_id`
      AND b.`number` = fb.`number`
      AND b.`style_number` = ''
      AND b.`flag` = 0
  );

-- ---------- 6. 回填 batch_id ----------
UPDATE `job` j
JOIN `batch` b
  ON b.`factory_id` = j.`factory_id`
 AND b.`number` = j.`number`
 AND b.`style_number` = IFNULL(j.`style_number`, '')
 AND b.`flag` = 0
SET j.`batch_id` = b.`id`
WHERE j.`batch_id` IS NULL;

UPDATE `job_work_order` o
JOIN `batch` b
  ON b.`factory_id` = o.`factory_id`
 AND b.`number` = o.`number`
 AND b.`style_number` = IFNULL(o.`style_number`, '')
 AND b.`flag` = 0
SET o.`batch_id` = b.`id`
WHERE o.`batch_id` IS NULL;

UPDATE `factory_bill` fb
JOIN `factory_piece_rate` fpr ON fpr.`id` = fb.`piece_rate_id`
JOIN `batch` b
  ON b.`factory_id` = fb.`factory_id`
 AND b.`number` = fb.`number`
 AND b.`style_number` = IFNULL(fpr.`style_number`, '')
 AND b.`flag` = 0
SET fb.`batch_id` = b.`id`
WHERE fb.`batch_id` IS NULL;

-- 没有计件单价的账单，挂到 5.5 建的空款式床上
UPDATE `factory_bill` fb
JOIN `batch` b
  ON b.`factory_id` = fb.`factory_id`
 AND b.`number` = fb.`number`
 AND b.`style_number` = ''
 AND b.`flag` = 0
SET fb.`batch_id` = b.`id`
WHERE fb.`batch_id` IS NULL;

-- ---------- 7. 验证（可以手动跑一下看看） ----------
-- 一共有多少张床
-- SELECT COUNT(*) AS batch_count FROM batch WHERE flag = 0;
-- 还有多少单据没挂上床（期望三行都是 0；不为 0 说明存在找不到计件单价的账单，先修数据）
-- SELECT 'job' AS tbl, COUNT(*) AS missing FROM job WHERE batch_id IS NULL AND flag = 0
-- UNION ALL SELECT 'job_work_order', COUNT(*) FROM job_work_order WHERE batch_id IS NULL AND flag = 0
-- UNION ALL SELECT 'factory_bill', COUNT(*) FROM factory_bill WHERE batch_id IS NULL AND flag = 0;
