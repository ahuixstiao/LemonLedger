-- ============================================================
-- 0.0.6：成衣厂账单结算单（导出快照 + 结清记录）
--
-- 背景：导出 Excel 之后就没人知道这一批到底结没结清；
--       结清与否只存在老板脑子里，翻旧账要重新导一遍。
-- 做法：每次导出账单时落一条结算单快照 + 每床明细，
--       结清时只在这条快照上记已付金额与付款日期。
-- ============================================================

CREATE TABLE IF NOT EXISTS `factory_bill_settlement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `factory_id` int NOT NULL COMMENT '成衣厂ID',
  `factory_name` varchar(64) DEFAULT NULL COMMENT '成衣厂名称（导出时快照）',
  `start_date` date DEFAULT NULL COMMENT '导出的起始日期',
  `end_date` date DEFAULT NULL COMMENT '导出的截止日期',
  `bill_count` int NOT NULL DEFAULT '0' COMMENT '本单账单笔数',
  `total_quantity` int NOT NULL DEFAULT '0' COMMENT '本单总件数',
  `total_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '本单总金额',
  `paid_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '已结金额',
  `paid_date` date DEFAULT NULL COMMENT '最近一次付款日期',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0未结清 1部分结清 2已结清',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `created_date` date DEFAULT NULL COMMENT '创建日期',
  `updated_date` date DEFAULT NULL COMMENT '最后修改日期',
  `flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除状态 0否 1是',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_settlement_factory` (`factory_id`, `created_date`),
  KEY `idx_settlement_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成衣厂账单结算单';

CREATE TABLE IF NOT EXISTS `factory_bill_settlement_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `settlement_id` bigint NOT NULL COMMENT '结算单ID',
  `bill_id` bigint DEFAULT NULL COMMENT '账单ID（导出那一刻）',
  `number` varchar(32) DEFAULT NULL COMMENT '床号',
  `style_number` varchar(64) DEFAULT NULL COMMENT '款式编号',
  `category` varchar(64) DEFAULT NULL COMMENT '工作类型',
  `quantity` int NOT NULL DEFAULT '0' COMMENT '数量',
  `price` decimal(10,4) DEFAULT NULL COMMENT '单价',
  `amount` decimal(12,2) DEFAULT NULL COMMENT '本床金额',
  `created_date` date DEFAULT NULL COMMENT '创建日期',
  `updated_date` date DEFAULT NULL COMMENT '最后修改日期',
  `flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除状态 0否 1是',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  KEY `idx_settlement_item` (`settlement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成衣厂账单结算单明细';

-- 校验：结算单汇总是否等于明细合计（正常应为 0 行）
-- SELECT s.id, s.total_amount, SUM(i.amount) AS item_amount
-- FROM factory_bill_settlement s
-- LEFT JOIN factory_bill_settlement_item i ON i.settlement_id = s.id AND i.flag = 0
-- WHERE s.flag = 0
-- GROUP BY s.id, s.total_amount
-- HAVING ABS(s.total_amount - COALESCE(SUM(i.amount), 0)) > 0.01;
