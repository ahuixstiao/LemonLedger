/*
 Navicat Premium Dump SQL

 Source Server         : localDataBase
 Source Server Type    : MySQL
 Source Server Version : 80021 (8.0.21)
 Source Host           : localhost:3306
 Source Schema         : ledger

 Target Server Type    : MySQL
 Target Server Version : 80021 (8.0.21)
 File Encoding         : 65001

 Date: 22/10/2025 23:35:55
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for employee
-- ----------------------------
DROP TABLE IF EXISTS `employee`;
CREATE TABLE `employee` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) NOT NULL COMMENT '员工姓名，用于查询工作内容',
  `mode_id` int NOT NULL COMMENT '工作模式 1压花 2刮胶',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `created_time` date DEFAULT NULL COMMENT '创建时间',
  `updated_by` varchar(64) DEFAULT NULL COMMENT '修改人',
  `updated_time` date COMMENT '更新时间',
  `flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除状态 0否 1是',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工表';

-- ----------------------------
-- Records of employee
-- ----------------------------
BEGIN;
INSERT INTO `employee` (`id`, `name`, `mode_id`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (1, '黄宝姨', 2, 0, NULL, '2025-10-03', NULL, '2025-10-03', 0, NULL);
INSERT INTO `employee` (`id`, `name`, `mode_id`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (2, '卢鑫辉', 1, 0, NULL, '2025-10-04', NULL, '2025-10-04', 0, NULL);
INSERT INTO `employee` (`id`, `name`, `mode_id`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (3, '黄兴林', 1, 0, NULL, '2025-10-16', NULL, '2025-10-16', 0, NULL);
INSERT INTO `employee` (`id`, `name`, `mode_id`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (4, '七弟', 1, 0, NULL, '2025-10-16', NULL, '2025-10-16', 0, NULL);
INSERT INTO `employee` (`id`, `name`, `mode_id`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (5, '梅', 2, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `employee` (`id`, `name`, `mode_id`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (6, '豪', 1, 0, NULL, '2025-10-17', NULL, NULL, 0, NULL);
INSERT INTO `employee` (`id`, `name`, `mode_id`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (7, '诗', 2, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `employee` (`id`, `name`, `mode_id`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (8, '张', 2, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
COMMIT;

-- ----------------------------
-- Table structure for factory
-- ----------------------------
DROP TABLE IF EXISTS `factory`;
CREATE TABLE `factory` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `factory_name` varchar(32) NOT NULL COMMENT '成衣厂名称',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `created_time` date DEFAULT NULL COMMENT '创建时间',
  `updated_by` varchar(64) DEFAULT NULL COMMENT '修改人',
  `updated_time` date COMMENT '修改时间',
  `flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除状态 0否 1是',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_factory_name` (`factory_name`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成衣厂表';

-- ----------------------------
-- Records of factory
-- ----------------------------
BEGIN;
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (1, '野狼传说', 0, NULL, '2025-10-03', NULL, '2025-10-03', 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (2, '王健', 0, NULL, '2025-10-03', NULL, '2025-10-03', 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (3, '金狐', 0, NULL, '2025-10-03', NULL, '2025-10-03', 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (4, '华', 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (5, '牧潮狼', 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (6, '朱永宁', 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (7, '皆旺', 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (8, '维格', 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (9, '九都龙', 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (10, '尚雅斯', 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (11, '文雪', 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (12, '魔鬼鸟', 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (13, '黎天', 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (14, '李华', 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (15, '博俊', 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (16, '黎永高', 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (19, '肖友标', 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (20, '增乐', 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (21, '邦狐', 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (22, '陈宗生', 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (23, '邹九', 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (24, '东方圣奴', 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (25, '李波', 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (26, '丘武', 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (27, '陈伟', 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (28, '凯维格', 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (29, '李春', 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (30, '冠霖', 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (31, '智博', 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (32, '酷之龙', 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (33, '李伍', 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
COMMIT;

-- ----------------------------
-- Table structure for factory_bill
-- ----------------------------
DROP TABLE IF EXISTS `factory_bill`;
CREATE TABLE `factory_bill` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `factory_id` int NOT NULL COMMENT '成衣厂ID',
  `number` varchar(32) NOT NULL COMMENT '床号',
  `style_number` int NOT NULL COMMENT '款式编号',
  `category_id` int DEFAULT NULL COMMENT '工作类型 1小花、2大花、3裤页',
  `quantity` int NOT NULL COMMENT '数量',
  `bill` decimal(8,2) DEFAULT NULL COMMENT '本床账单',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `created_time` date NOT NULL COMMENT '创建时间',
  `updated_by` varchar(64) DEFAULT NULL COMMENT '修改人',
  `updated_time` date COMMENT '修改时间',
  `flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除状态 0否 1是',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_factory_bill` (`factory_id`,`number`,`style_number`,`flag`,`created_time`),
  KEY `factory_bill_number_index` (`number`),
  KEY `factory_bill_style_number_index` (`style_number`)
) ENGINE=InnoDB AUTO_INCREMENT=109 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成衣厂账单表';

-- ----------------------------
-- Records of factory_bill
-- ----------------------------
BEGIN;
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (28, 2, '162', 602, NULL, 1254, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (29, 7, '33', 0, NULL, 560, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (30, 7, '34', 0, NULL, 672, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (31, 8, '63', 0, NULL, 672, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (32, 6, '31', 0, NULL, 102, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (33, 6, '33', 0, NULL, 98, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (34, 6, '34', 0, NULL, 574, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (35, 10, '175', 0, NULL, 598, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (42, 11, '543-548', 0, NULL, 672, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (43, 12, '51-3', 0, NULL, 178, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (44, 12, '51-2', 0, NULL, 178, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (45, 13, '25', 0, NULL, 220, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (46, 2, '5', 2001, NULL, 440, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (47, 2, '6', 2001, NULL, 455, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (48, 14, '32', 0, NULL, 980, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (49, 1, '42', 9588, NULL, 720, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (50, 9, '45', 0, NULL, 1054, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (51, 15, '23', 9212, NULL, 714, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (52, 15, '65-66', 9212, NULL, 1596, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (53, 28, '245', 0, NULL, 760, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (54, 28, '255', 0, NULL, 294, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (55, 6, '35', 0, NULL, 728, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (56, 3, '66', 6062, NULL, 882, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (57, 2, '1', 3015, NULL, 804, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (58, 2, '3', 3009, NULL, 374, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (59, 2, '4', 3009, NULL, 275, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (60, 19, '53', 0, NULL, 689, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (61, 16, '85', 825, NULL, 686, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (62, 15, '47', 9203, NULL, 1512, NULL, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (63, 4, '23', 0, NULL, 468, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (64, 29, '37', 9917, NULL, 340, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (65, 25, '265', 0, NULL, 1280, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (66, 27, '12', 0, NULL, 535, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (67, 30, '82', 0, NULL, 780, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (68, 31, '82', 0, NULL, 702, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (69, 24, '43', 0, NULL, 453, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (70, 24, '44', 0, NULL, 216, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (71, 25, '264', 0, NULL, 610, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (72, 32, '34', 0, NULL, 440, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (74, 32, '35', 0, NULL, 420, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (76, 4, '24', 0, NULL, 432, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (78, 5, '136', 0, NULL, 560, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (80, 27, '11', 0, NULL, 602, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (81, 6, '30', 0, NULL, 289, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (82, 6, '32', 0, NULL, 66, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (83, 11, '536-542', 0, NULL, 684, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (84, 12, '52-4', 0, NULL, 178, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (85, 3, '64', 0, NULL, 616, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (86, 3, '65', 0, NULL, 182, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (87, 16, '80', 925, NULL, 490, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (88, 16, '82', 0, NULL, 686, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (89, 16, '81', 925, NULL, 308, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (90, 26, '33', 0, NULL, 412, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (91, 22, '34', 0, NULL, 300, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (92, 22, '33', 0, NULL, 412, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (93, 15, '35', 9121, NULL, 1260, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (94, 29, '38', 0, NULL, 440, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (95, 29, '40', 0, NULL, 340, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (96, 1, '3', 9585, NULL, 574, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (97, 10, '174', 0, NULL, 660, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (98, 5, '138', 0, NULL, 385, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (99, 5, '139', 0, NULL, 198, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (100, 22, '178', 0, NULL, 716, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (101, 1, '41', 9588, NULL, 700, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (102, 16, '83', 0, NULL, 812, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (103, 1, '40', 7763, NULL, 410, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (104, 20, '64', 0, NULL, 888, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (105, 8, '62', 0, NULL, 612, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (106, 21, '32', 0, NULL, 506, NULL, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (107, 33, '165', 0, NULL, 361, NULL, 0, NULL, '2025-10-20', NULL, NULL, 0, NULL);
COMMIT;

-- ----------------------------
-- Table structure for factory_quotation
-- ----------------------------
DROP TABLE IF EXISTS `factory_quotation`;
CREATE TABLE `factory_quotation` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `factory_id` int NOT NULL COMMENT '成衣厂ID',
  `category_id` int NOT NULL COMMENT '工作类型 1小花、2大花、3裤页',
  `quotation` decimal(8,2) NOT NULL COMMENT '报价',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `created_time` date DEFAULT NULL COMMENT '创建时间',
  `updated_by` varchar(64) DEFAULT NULL COMMENT '修改人',
  `updated_time` date COMMENT '修改时间',
  `flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除状态 0否 1是',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成衣厂报价表';

-- ----------------------------
-- Records of factory_quotation
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for job
-- ----------------------------
DROP TABLE IF EXISTS `job`;
CREATE TABLE `job` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `employee_id` int NOT NULL COMMENT '员工ID',
  `factory_id` int NOT NULL COMMENT '成衣厂ID',
  `number` varchar(32) NOT NULL COMMENT '床号',
  `style_number` int DEFAULT NULL COMMENT '款式编号 比如3101',
  `mode_id` int NOT NULL COMMENT '工作方式，例如 压花、刮胶',
  `category_id` int NOT NULL COMMENT '工作类型，例如 小花、大花、裤页',
  `quantity` int NOT NULL COMMENT '数量',
  `salary` decimal(8,2) NOT NULL COMMENT '本床薪资',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `created_time` date NOT NULL COMMENT '创建时间',
  `updated_by` varchar(64) DEFAULT NULL COMMENT '修改人',
  `updated_time` date COMMENT '更新时间',
  `flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除状态 0否 1是',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_job_info` (`employee_id`,`factory_id`,`category_id`,`mode_id`,`style_number`,`number`,`flag`,`created_time`),
  KEY `job_category_id_index` (`category_id`),
  KEY `job_employee_id_index` (`employee_id`),
  KEY `job_factory_id_index` (`factory_id`)
) ENGINE=InnoDB AUTO_INCREMENT=311 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作内容表';

-- ----------------------------
-- Records of job
-- ----------------------------
BEGIN;
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (158, 6, 2, '162', 602, 1, 1, 1254, 50.16, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (159, 5, 2, '162', 602, 2, 1, 1254, 50.16, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (160, 6, 7, '33', 0, 1, 1, 560, 22.40, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (161, 5, 7, '33', 0, 2, 1, 560, 22.40, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (162, 5, 7, '34', 0, 2, 1, 672, 26.88, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (163, 6, 7, '34', 0, 1, 1, 672, 26.88, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (164, 4, 8, '63', 0, 1, 1, 672, 26.88, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (165, 7, 8, '63', 0, 2, 1, 672, 26.88, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (166, 7, 6, '31', 0, 2, 1, 102, 4.08, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (167, 4, 6, '31', 0, 1, 1, 102, 4.08, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (168, 4, 6, '33', 0, 1, 1, 98, 3.92, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (169, 4, 6, '34', 0, 1, 1, 574, 22.96, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (170, 7, 6, '34', 0, 2, 1, 574, 22.96, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (171, 7, 6, '33', 0, 2, 1, 98, 3.92, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (172, 2, 10, '175', 0, 1, 1, 598, 23.92, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (173, 1, 10, '175', 0, 2, 1, 598, 23.92, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (174, 1, 11, '543', 0, 2, 1, 112, 4.48, 0, NULL, '2025-10-22', NULL, '2025-10-22', 1, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (175, 1, 11, '544', 0, 2, 1, 112, 4.48, 0, NULL, '2025-10-22', NULL, '2025-10-22', 1, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (176, 1, 11, '545', 0, 2, 1, 112, 4.48, 0, NULL, '2025-10-22', NULL, '2025-10-22', 1, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (177, 1, 11, '546', 0, 2, 1, 112, 4.48, 0, NULL, '2025-10-22', NULL, '2025-10-22', 1, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (178, 1, 11, '547', 0, 2, 1, 112, 4.48, 0, NULL, '2025-10-22', NULL, '2025-10-22', 1, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (179, 1, 11, '548', 0, 2, 1, 112, 4.48, 0, NULL, '2025-10-22', NULL, '2025-10-22', 1, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (180, 2, 11, '543', 0, 1, 1, 112, 4.48, 0, NULL, '2025-10-22', NULL, '2025-10-22', 1, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (181, 2, 11, '544', 0, 1, 1, 112, 4.48, 0, NULL, '2025-10-22', NULL, '2025-10-22', 1, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (182, 2, 11, '545', 0, 1, 1, 112, 4.48, 0, NULL, '2025-10-22', NULL, '2025-10-22', 1, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (183, 2, 11, '546', 0, 1, 1, 112, 4.48, 0, NULL, '2025-10-22', NULL, '2025-10-22', 1, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (184, 2, 11, '547', 0, 1, 1, 112, 4.48, 0, NULL, '2025-10-22', NULL, '2025-10-22', 1, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (185, 2, 11, '548', 0, 1, 1, 112, 4.48, 0, NULL, '2025-10-22', NULL, '2025-10-22', 1, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (186, 1, 11, '543-548', 0, 2, 1, 672, 26.88, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (187, 2, 11, '543-548', 0, 1, 1, 672, 26.88, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (188, 4, 12, '51-3', 0, 1, 2, 178, 7.12, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (189, 7, 12, '51-3', 0, 2, 2, 178, 10.68, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (190, 7, 12, '51-2', 0, 2, 2, 178, 10.68, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (191, 4, 12, '51-2', 0, 1, 2, 178, 7.12, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (192, 2, 13, '25', 0, 1, 1, 220, 8.80, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (193, 1, 13, '25', 0, 2, 1, 220, 8.80, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (194, 3, 2, '5', 2001, 1, 1, 440, 17.60, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (195, 3, 2, '6', 2001, 1, 1, 455, 18.20, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (196, 8, 2, '5', 2001, 2, 1, 440, 17.60, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (197, 8, 2, '6', 2001, 2, 1, 455, 18.20, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (198, 6, 14, '32', 0, 1, 1, 980, 39.20, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (199, 5, 14, '32', 0, 2, 1, 980, 39.20, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (200, 3, 1, '42', 9588, 1, 1, 720, 28.80, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (201, 8, 1, '42', 9588, 2, 1, 720, 28.80, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (202, 4, 9, '45', 0, 1, 1, 1054, 42.16, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (203, 7, 9, '45', 0, 2, 1, 1054, 42.16, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (204, 2, 15, '23', 9212, 1, 1, 714, 28.56, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (205, 1, 15, '23', 9212, 2, 1, 714, 28.56, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (206, 1, 15, '65-66', 9212, 2, 1, 1596, 63.84, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (207, 2, 15, '65-66', 9212, 1, 1, 1596, 63.84, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (208, 6, 28, '245', 0, 1, 1, 760, 30.40, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (209, 5, 28, '245', 0, 2, 1, 760, 30.40, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (210, 5, 28, '255', 0, 2, 1, 294, 11.76, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (211, 6, 28, '255', 0, 1, 1, 294, 11.76, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (212, 2, 6, '35', 0, 1, 1, 728, 29.12, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (213, 1, 6, '35', 0, 2, 1, 728, 29.12, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (214, 2, 3, '66', 6062, 1, 1, 882, 35.28, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (215, 1, 3, '66', 6062, 2, 1, 882, 35.28, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (216, 2, 2, '1', 3015, 1, 1, 804, 32.16, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (217, 1, 2, '1', 3015, 2, 1, 804, 32.16, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (218, 4, 2, '3', 3009, 1, 1, 374, 14.96, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (219, 4, 2, '4', 3009, 1, 1, 275, 11.00, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (220, 7, 2, '4', 3009, 2, 1, 275, 11.00, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (221, 7, 2, '3', 3009, 2, 1, 374, 14.96, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (222, 3, 19, '53', 0, 1, 1, 689, 27.56, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (223, 8, 19, '53', 0, 2, 1, 689, 27.56, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (224, 8, 16, '85', 825, 2, 1, 686, 27.44, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (225, 3, 16, '85', 825, 1, 1, 686, 27.44, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (226, 6, 15, '47', 9203, 1, 1, 1512, 60.48, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (227, 5, 15, '47', 9203, 2, 1, 1512, 60.48, 0, NULL, '2025-10-22', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (228, 5, 4, '23', 0, 2, 1, 468, 18.72, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (229, 6, 4, '23', 0, 1, 1, 468, 18.72, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (230, 6, 29, '37', 9917, 1, 1, 340, 13.60, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (231, 5, 29, '37', 9917, 2, 1, 340, 13.60, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (232, 5, 25, '265', 0, 2, 1, 1280, 51.20, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (233, 6, 25, '265', 0, 1, 1, 1280, 51.20, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (234, 5, 27, '12', 0, 2, 1, 535, 21.40, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (235, 5, 30, '82', 0, 2, 1, 780, 31.20, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (236, 6, 30, '82', 0, 1, 1, 780, 31.20, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (237, 3, 31, '82', 0, 1, 2, 702, 28.08, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (238, 5, 31, '82', 0, 2, 2, 190, 11.40, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (239, 8, 31, '82', 0, 2, 2, 256, 15.36, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (240, 1, 31, '82', 0, 2, 2, 256, 15.36, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (241, 7, 24, '43', 0, 2, 1, 453, 18.12, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (242, 4, 24, '43', 0, 1, 1, 453, 18.12, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (243, 4, 24, '44', 0, 1, 1, 216, 8.64, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (244, 7, 24, '44', 0, 2, 1, 216, 8.64, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (245, 3, 25, '264', 0, 1, 1, 610, 24.40, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (246, 1, 25, '264', 0, 2, 1, 610, 24.40, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (247, 3, 27, '12', 0, 1, 1, 535, 21.40, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (254, 2, 32, '34', 0, 1, 1, 440, 17.60, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (255, 1, 32, '34', 0, 2, 1, 440, 17.60, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (256, 4, 32, '35', 0, 1, 1, 420, 16.80, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (257, 7, 32, '35', 0, 2, 1, 420, 16.80, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (258, 2, 4, '24', 0, 1, 1, 432, 17.28, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (259, 1, 4, '24', 0, 2, 1, 432, 17.28, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (260, 4, 5, '136', 0, 1, 1, 560, 22.40, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (261, 7, 5, '136', 0, 2, 1, 560, 22.40, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (262, 1, 27, '11', 0, 2, 1, 602, 24.08, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (263, 3, 27, '11', 0, 1, 1, 602, 24.08, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (264, 2, 6, '30', 0, 1, 1, 289, 11.56, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (265, 1, 6, '30', 0, 2, 1, 289, 11.56, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (266, 1, 6, '32', 0, 2, 1, 66, 2.64, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (267, 2, 6, '32', 0, 1, 1, 66, 2.64, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (268, 2, 11, '536-542', 0, 1, 1, 684, 27.36, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (269, 1, 11, '536-542', 0, 2, 1, 684, 27.36, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (270, 4, 12, '52-4', 0, 1, 2, 178, 7.12, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (271, 7, 12, '52-4', 0, 2, 2, 178, 10.68, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (272, 7, 3, '64', 0, 2, 1, 616, 24.64, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (273, 4, 3, '64', 0, 1, 1, 616, 24.64, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (274, 4, 3, '65', 0, 1, 1, 182, 7.28, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (275, 7, 3, '65', 0, 2, 1, 182, 7.28, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (276, 6, 16, '80', 925, 1, 1, 490, 19.60, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (277, 3, 16, '82', 0, 1, 1, 686, 27.44, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (278, 6, 16, '81', 925, 1, 1, 308, 12.32, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (279, 6, 26, '33', 0, 1, 1, 412, 16.48, 0, NULL, '2025-10-21', NULL, '2025-10-22', 1, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (280, 6, 22, '34', 0, 1, 1, 300, 12.00, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (281, 6, 22, '33', 0, 1, 1, 412, 16.48, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (282, 2, 15, '35', 9121, 1, 1, 1260, 50.40, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (283, 1, 15, '35', 9121, 2, 1, 1260, 50.40, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (284, 4, 29, '38', 0, 1, 1, 440, 17.60, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (285, 4, 29, '40', 0, 1, 1, 340, 13.60, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (286, 7, 29, '40', 0, 2, 1, 340, 13.60, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (287, 7, 29, '38', 0, 2, 1, 440, 17.60, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (288, 3, 1, '3', 9585, 1, 1, 574, 22.96, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (289, 8, 1, '3', 9585, 2, 1, 574, 22.96, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (290, 2, 10, '174', 0, 1, 1, 660, 26.40, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (291, 1, 10, '174', 0, 2, 1, 660, 26.40, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (292, 3, 5, '138', 0, 1, 1, 385, 15.40, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (293, 8, 5, '138', 0, 2, 1, 385, 15.40, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (294, 8, 5, '139', 0, 2, 1, 198, 7.92, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (295, 3, 5, '139', 0, 1, 1, 198, 7.92, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (296, 6, 22, '178', 0, 1, 1, 716, 28.64, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (297, 3, 1, '41', 9588, 1, 1, 700, 28.00, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (298, 8, 1, '41', 9588, 2, 1, 700, 28.00, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (299, 3, 16, '83', 0, 1, 1, 812, 32.48, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (300, 8, 16, '83', 0, 2, 1, 812, 32.48, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (301, 4, 1, '40', 7763, 1, 1, 410, 16.40, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (302, 7, 1, '40', 7763, 2, 1, 410, 16.40, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (303, 7, 20, '64', 0, 2, 1, 888, 35.52, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (304, 4, 20, '64', 0, 1, 1, 888, 35.52, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (305, 2, 8, '62', 0, 1, 1, 612, 24.48, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (306, 1, 8, '62', 0, 2, 1, 612, 24.48, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (307, 1, 21, '32', 0, 2, 1, 506, 20.24, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (308, 2, 21, '32', 0, 1, 1, 506, 20.24, 0, NULL, '2025-10-21', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (309, 5, 33, '165', 0, 2, 1, 361, 14.44, 0, NULL, '2025-10-20', NULL, NULL, 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (310, 6, 33, '165', 0, 1, 1, 361, 14.44, 0, NULL, '2025-10-20', NULL, NULL, 0, NULL);
COMMIT;

-- ----------------------------
-- Table structure for job_category
-- ----------------------------
DROP TABLE IF EXISTS `job_category`;
CREATE TABLE `job_category` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `category` varchar(32) NOT NULL COMMENT '工作类型，例如 小花、大花、裤页',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `created_time` date NOT NULL COMMENT '创建时间',
  `updated_by` varchar(64) DEFAULT NULL COMMENT '修改人',
  `updated_time` date COMMENT '更新时间',
  `flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除状态 0否 1是',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作类型表';

-- ----------------------------
-- Records of job_category
-- ----------------------------
BEGIN;
INSERT INTO `job_category` (`id`, `category`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (1, '小花', 0, NULL, '2025-10-03', NULL, '2025-10-03', 0, NULL);
INSERT INTO `job_category` (`id`, `category`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (2, '大花', 0, NULL, '2025-10-03', NULL, '2025-10-03', 0, NULL);
INSERT INTO `job_category` (`id`, `category`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (3, '裤页', 0, NULL, '2025-10-03', NULL, '2025-10-03', 0, NULL);
COMMIT;

-- ----------------------------
-- Table structure for job_mode
-- ----------------------------
DROP TABLE IF EXISTS `job_mode`;
CREATE TABLE `job_mode` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `mode` varchar(32) NOT NULL COMMENT '工作方式，例如 1压花、2刮胶 3xxx',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `created_time` date NOT NULL COMMENT '创建时间',
  `updated_by` varchar(64) DEFAULT NULL COMMENT '修改人',
  `updated_time` date COMMENT '更新时间',
  `flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除状态 0否 1是',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作方式表';

-- ----------------------------
-- Records of job_mode
-- ----------------------------
BEGIN;
INSERT INTO `job_mode` (`id`, `mode`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (1, '压花', 0, 'admin', '2025-10-04', NULL, '2025-10-04', 0, NULL);
INSERT INTO `job_mode` (`id`, `mode`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (2, '刮胶', 0, 'admin', '2025-10-04', NULL, '2025-10-04', 0, NULL);
COMMIT;

-- ----------------------------
-- Table structure for job_quotation
-- ----------------------------
DROP TABLE IF EXISTS `job_quotation`;
CREATE TABLE `job_quotation` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `mode_id` int NOT NULL COMMENT '工作方式',
  `category_id` int NOT NULL COMMENT '工作类型 1小花、2大花、3裤页',
  `quotation` decimal(8,2) NOT NULL COMMENT '报价',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `created_time` date NOT NULL COMMENT '创建时间',
  `updated_by` varchar(64) DEFAULT NULL COMMENT '修改人',
  `updated_time` date COMMENT '修改时间',
  `flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除状态 0否 1是',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `job_quotation_job_category_id_fk` (`category_id`),
  KEY `job_quotation_job_mode_id_fk` (`mode_id`),
  CONSTRAINT `job_quotation_job_category_id_fk` FOREIGN KEY (`category_id`) REFERENCES `job_category` (`id`) ON DELETE CASCADE,
  CONSTRAINT `job_quotation_job_mode_id_fk` FOREIGN KEY (`mode_id`) REFERENCES `job_mode` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作报价表';

-- ----------------------------
-- Records of job_quotation
-- ----------------------------
BEGIN;
INSERT INTO `job_quotation` (`id`, `mode_id`, `category_id`, `quotation`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (1, 1, 1, 0.04, 0, NULL, '2025-10-16', NULL, '2025-10-16', 0, NULL);
INSERT INTO `job_quotation` (`id`, `mode_id`, `category_id`, `quotation`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (2, 1, 2, 0.04, 0, NULL, '2025-10-16', NULL, '2025-10-16', 0, NULL);
INSERT INTO `job_quotation` (`id`, `mode_id`, `category_id`, `quotation`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (3, 1, 3, 0.08, 0, NULL, '2025-10-16', NULL, '2025-10-16', 0, NULL);
INSERT INTO `job_quotation` (`id`, `mode_id`, `category_id`, `quotation`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (4, 2, 1, 0.04, 0, NULL, '2025-10-16', NULL, '2025-10-16', 0, NULL);
INSERT INTO `job_quotation` (`id`, `mode_id`, `category_id`, `quotation`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (5, 2, 2, 0.06, 0, NULL, '2025-10-16', NULL, '2025-10-16', 0, NULL);
INSERT INTO `job_quotation` (`id`, `mode_id`, `category_id`, `quotation`, `version`, `created_by`, `created_time`, `updated_by`, `updated_time`, `flag`, `remark`) VALUES (6, 2, 3, 0.10, 0, NULL, '2025-10-16', NULL, '2025-10-16', 0, NULL);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
