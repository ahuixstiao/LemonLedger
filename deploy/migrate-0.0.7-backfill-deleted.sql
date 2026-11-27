-- 统一连接字符集，避免中文注释被按 latin1 存成乱码
SET NAMES utf8mb4;

-- 0.0.7 补丁：把被软删除的计件单价也能挂到款式上
-- 1) 为所有没进款式表的「成衣厂 + 款式编号」补一条款式记录；
--    只有当这对组合下的所有单价都被删除时，补出来的款式也标记为删除。
INSERT INTO factory_style (factory_id, style_number, image_path, created_date, updated_date, flag)
SELECT
    fp.factory_id,
    fp.style_number,
    SUBSTRING_INDEX(GROUP_CONCAT(NULLIF(fp.image_path, '') ORDER BY fp.id DESC), ',', 1),
    MIN(fp.created_date),
    MAX(fp.updated_date),
    CASE WHEN SUM(CASE WHEN fp.flag = 0 THEN 1 ELSE 0 END) = 0 THEN 1 ELSE 0 END
FROM factory_piece_rate fp
WHERE fp.style_number IS NOT NULL
  AND fp.style_number <> ''
  AND NOT EXISTS (
      SELECT 1 FROM factory_style fs
      WHERE fs.factory_id = fp.factory_id
        AND fs.style_number = fp.style_number
        AND fs.flag = 0
  )
GROUP BY fp.factory_id, fp.style_number;

-- 2) 再回填一次，这次允许挂到「已删除」的款式上
UPDATE factory_piece_rate fp
JOIN factory_style fs
  ON fs.factory_id = fp.factory_id
 AND fs.style_number = fp.style_number
SET fp.style_id = fs.id
WHERE fp.style_id IS NULL;

-- 3) 复查：应为 0
SELECT COUNT(*) AS unmapped_rows FROM factory_piece_rate WHERE style_id IS NULL;
