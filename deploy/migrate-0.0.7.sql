-- ============================================================
-- 0.0.7：把「款式编号 + 样板图片」从成衣厂计件单价表里抽出来，
--        单独建一张成衣厂款式表，计件单价表改为只引用款式ID。
-- 执行前提：已备份数据库（deploy/backup-mysql.sh），本脚本也会先备份单价表。
-- ============================================================

-- 1. 备份原计件单价表（含 style_number / image_path 两列，出问题可回滚）
CREATE TABLE IF NOT EXISTS factory_piece_rate_backup_007 AS
SELECT * FROM factory_piece_rate;

-- 2. 成衣厂款式表：一个成衣厂 + 一个款式编号 = 一条，图片存在这里
CREATE TABLE IF NOT EXISTS factory_style (
  id int NOT NULL AUTO_INCREMENT COMMENT '主键',
  factory_id int NOT NULL COMMENT '成衣厂ID',
  style_number varchar(32) NOT NULL COMMENT '款式编号',
  image_path varchar(512) DEFAULT NULL COMMENT '样板图片访问路径',
  version int DEFAULT '0' COMMENT '乐观锁版本',
  created_date date DEFAULT NULL COMMENT '创建日期',
  updated_date date DEFAULT NULL COMMENT '修改日期',
  flag tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除状态 0正常 1删除',
  remark varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_factory_style (factory_id, style_number, flag),
  KEY idx_factory_style_factory (factory_id, flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成衣厂款式表（款式编号与样板图片）';

-- 3. 把单价表里出现过的「成衣厂 + 款式编号」搬进款式表，
--    图片取该款式下最新一条不为空的（老数据同一款式多工种可能各存了一份图）
INSERT INTO factory_style (factory_id, style_number, image_path, created_date, updated_date, flag)
SELECT
    fp.factory_id,
    fp.style_number,
    latest.image_path,
    MIN(fp.created_date),
    MAX(fp.updated_date),
    0
FROM factory_piece_rate fp
JOIN (
    SELECT factory_id, style_number,
           SUBSTRING_INDEX(GROUP_CONCAT(NULLIF(image_path, '') ORDER BY id DESC), ',', 1) AS image_path
    FROM factory_piece_rate
    WHERE flag = 0
    GROUP BY factory_id, style_number
) latest
  ON latest.factory_id = fp.factory_id AND latest.style_number = fp.style_number
WHERE fp.flag = 0
  AND fp.style_number IS NOT NULL
  AND fp.style_number <> ''
GROUP BY fp.factory_id, fp.style_number, latest.image_path;

-- 4. 单价表加 style_id 并按「厂 + 款式编号」回填
ALTER TABLE factory_piece_rate ADD COLUMN style_id int NULL COMMENT '成衣厂款式ID' AFTER factory_id;

UPDATE factory_piece_rate fp
JOIN factory_style fs
  ON fs.factory_id = fp.factory_id
 AND fs.style_number = fp.style_number
 AND fs.flag = 0
SET fp.style_id = fs.id
WHERE fp.style_id IS NULL;

-- 5. 兜底：理论上不该有漏网的，有就报错让执行者先人工确认
SELECT COUNT(*) AS unmapped_rows FROM factory_piece_rate WHERE style_id IS NULL;

-- 6. 删掉旧的两列，style_id 收紧为非空并加索引（确认第 5 步为 0 再执行）
ALTER TABLE factory_piece_rate
    DROP COLUMN style_number,
    DROP COLUMN image_path,
    MODIFY COLUMN style_id int NOT NULL COMMENT '成衣厂款式ID',
    ADD KEY idx_factory_piece_rate_style (style_id);

-- 7. 校验：款式条数应等于迁移前「成衣厂 + 款式编号」的组合数，且单价都能对上款式
SELECT COUNT(*) AS style_rows FROM factory_style WHERE flag = 0;
SELECT COUNT(*) AS rate_rows, COUNT(DISTINCT style_id) AS style_used FROM factory_piece_rate;
