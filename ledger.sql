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

 Date: 07/12/2025 01:43:46
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
  `created_date` date DEFAULT NULL COMMENT '创建日期',
  `flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除状态 0否 1是',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工表';

-- ----------------------------
-- Records of employee
-- ----------------------------
BEGIN;
INSERT INTO `employee` (`id`, `name`, `mode_id`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (1, '黄宝姨', 2, 0, NULL, '2025-10-03', 0, NULL);
INSERT INTO `employee` (`id`, `name`, `mode_id`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (2, '卢鑫辉', 1, 0, NULL, '2025-10-04', 0, NULL);
INSERT INTO `employee` (`id`, `name`, `mode_id`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (3, '黄兴林', 1, 0, NULL, '2025-10-16', 0, NULL);
INSERT INTO `employee` (`id`, `name`, `mode_id`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (4, '七弟', 1, 0, NULL, '2025-10-16', 0, NULL);
INSERT INTO `employee` (`id`, `name`, `mode_id`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (5, '梅', 2, 0, NULL, '2025-10-21', 0, NULL);
INSERT INTO `employee` (`id`, `name`, `mode_id`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (6, '豪', 1, 0, NULL, '2025-10-17', 0, NULL);
INSERT INTO `employee` (`id`, `name`, `mode_id`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (7, '诗', 2, 0, NULL, NULL, 0, NULL);
INSERT INTO `employee` (`id`, `name`, `mode_id`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (8, '张', 2, 0, NULL, NULL, 0, NULL);
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
  `created_date` date DEFAULT NULL COMMENT '创建日期',
  `flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除状态 0否 1是',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_factory_name` (`factory_name`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成衣厂表';

-- ----------------------------
-- Records of factory
-- ----------------------------
BEGIN;
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (1, '野狼传说', 0, NULL, '2025-10-03', 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (2, '王健', 0, NULL, '2025-10-03', 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (3, '金狐', 0, NULL, '2025-10-03', 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (4, '华', 0, NULL, '2025-10-21', 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (5, '牧潮狼', 0, NULL, '2025-10-21', 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (6, '朱永宁', 0, NULL, '2025-10-21', 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (7, '皆旺', 0, NULL, '2025-10-22', 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (8, '维格', 0, NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (9, '九都龙', 0, NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (10, '尚雅斯', 0, NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (11, '文雪', 0, NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (12, '魔鬼鸟', 0, NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (13, '黎天', 0, NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (14, '李华', 0, NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (15, '博俊', 0, NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (16, '黎永高', 0, NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (19, '肖友标', 0, NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (20, '增乐', 0, NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (21, '邦狐', 0, NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (22, '陈宗生', 0, NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (23, '邹九', 0, NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (24, '东方圣奴', 0, NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (25, '李波', 0, NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (26, '丘武', 0, NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (27, '陈伟', 0, NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (28, '凯维格', 0, NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (29, '李春', 0, NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (30, '冠霖', 0, NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (31, '智博', 0, NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (32, '酷之龙', 0, NULL, NULL, 0, NULL);
INSERT INTO `factory` (`id`, `factory_name`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (33, '李伍', 0, NULL, NULL, 0, NULL);
COMMIT;

-- ----------------------------
-- Table structure for factory_bill
-- ----------------------------
DROP TABLE IF EXISTS `factory_bill`;
CREATE TABLE `factory_bill` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `factory_id` int NOT NULL COMMENT '成衣厂ID',
  `number` varchar(32) NOT NULL COMMENT '床号',
  `style_number` varchar(32) NOT NULL COMMENT '款式编号',
  `category_id` int DEFAULT NULL COMMENT '工作类型 1小花、2大花、3裤页',
  `quantity` int NOT NULL COMMENT '数量',
  `bill` decimal(8,2) DEFAULT NULL COMMENT '本床账单',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `created_date` date NOT NULL COMMENT '创建日期',
  `flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除状态 0否 1是',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `factory_bill_number_index` (`number`),
  KEY `factory_bill_style_number_index` (`style_number`)
) ENGINE=InnoDB AUTO_INCREMENT=133 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成衣厂账单表';

-- ----------------------------
-- Records of factory_bill
-- ----------------------------
BEGIN;
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_date`, `flag`, `remark`) VALUES (123, 1, '111', '222', 1, 333, 111.00, 0, '2025-11-03', 1, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_date`, `flag`, `remark`) VALUES (124, 1, '111', '222', 2, 333, 222.00, 0, '2025-11-03', 1, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_date`, `flag`, `remark`) VALUES (125, 1, '111', '222', 2, 333, 333.00, 0, '2025-11-03', 1, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_date`, `flag`, `remark`) VALUES (126, 1, '111', '222', 1, 333, 444.00, 0, '2025-11-03', 1, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_date`, `flag`, `remark`) VALUES (127, 1, '111', '222', 1, 333, 555.00, 0, '2025-11-03', 1, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_date`, `flag`, `remark`) VALUES (128, 1, '111', '7784', 1, 333, 266.40, 0, '2025-11-03', 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_date`, `flag`, `remark`) VALUES (129, 1, '222', '333', 1, 4444, 777.00, 0, '2025-11-03', 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_date`, `flag`, `remark`) VALUES (130, 1, '232', '444', 1, 222, 88.00, 0, '2025-11-03', 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_date`, `flag`, `remark`) VALUES (131, 3, '222', '222', 1, 222, 444.00, 0, '2025-11-03', 0, NULL);
INSERT INTO `factory_bill` (`id`, `factory_id`, `number`, `style_number`, `category_id`, `quantity`, `bill`, `version`, `created_date`, `flag`, `remark`) VALUES (132, 1, '34', '7784', 3, 1000, 800.00, 0, '2025-12-07', 0, NULL);
COMMIT;

-- ----------------------------
-- Table structure for factory_quotation
-- ----------------------------
DROP TABLE IF EXISTS `factory_quotation`;
CREATE TABLE `factory_quotation` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `factory_id` int NOT NULL COMMENT '成衣厂ID',
  `style_number` varchar(32) DEFAULT NULL COMMENT '款式编号',
  `category_id` int NOT NULL COMMENT '工作类型 1小花、2大花、3裤页',
  `quotation` decimal(8,2) NOT NULL COMMENT '单个报价',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `created_date` date DEFAULT NULL COMMENT '创建日期',
  `flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除状态 0否 1是',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_factory_quotation` (`factory_id`,`style_number`,`category_id`,`quotation`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成衣厂报价表';

-- ----------------------------
-- Records of factory_quotation
-- ----------------------------
BEGIN;
INSERT INTO `factory_quotation` (`id`, `factory_id`, `style_number`, `category_id`, `quotation`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (10, 1, '9595', 1, 0.25, 0, NULL, NULL, 0, NULL);
INSERT INTO `factory_quotation` (`id`, `factory_id`, `style_number`, `category_id`, `quotation`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (11, 1, '8721', 2, 0.40, 0, NULL, NULL, 0, NULL);
INSERT INTO `factory_quotation` (`id`, `factory_id`, `style_number`, `category_id`, `quotation`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (12, 1, '7784', 3, 0.80, 0, NULL, NULL, 0, NULL);
INSERT INTO `factory_quotation` (`id`, `factory_id`, `style_number`, `category_id`, `quotation`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (13, 2, '1113', 1, 0.30, 0, NULL, NULL, 0, NULL);
INSERT INTO `factory_quotation` (`id`, `factory_id`, `style_number`, `category_id`, `quotation`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (14, 2, '9313', 2, 0.50, 0, NULL, NULL, 0, NULL);
INSERT INTO `factory_quotation` (`id`, `factory_id`, `style_number`, `category_id`, `quotation`, `version`, `created_by`, `created_date`, `flag`, `remark`) VALUES (15, 2, '9877', 3, 0.90, 0, NULL, NULL, 0, NULL);
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
  `style_number` varchar(32) DEFAULT NULL COMMENT '款式编号 比如3101',
  `mode_id` int NOT NULL COMMENT '工作方式，例如 压花、刮胶',
  `category_id` int NOT NULL COMMENT '工作类型，例如 小花、大花、裤页',
  `quantity` int NOT NULL COMMENT '数量',
  `salary` decimal(8,2) NOT NULL COMMENT '本床薪资',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `created_date` date NOT NULL COMMENT '创建日期',
  `flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除状态 0否 1是',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `job_category_id_index` (`category_id`),
  KEY `job_employee_id_index` (`employee_id`),
  KEY `job_factory_id_index` (`factory_id`)
) ENGINE=InnoDB AUTO_INCREMENT=364 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作内容表';

-- ----------------------------
-- Records of job
-- ----------------------------
BEGIN;
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_date`, `flag`, `remark`) VALUES (344, 1, 1, '111', '222', 2, 1, 333, 13.32, 0, '2025-11-03', 1, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_date`, `flag`, `remark`) VALUES (345, 2, 1, '111', '222', 1, 1, 333, 13.32, 0, '2025-11-03', 1, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_date`, `flag`, `remark`) VALUES (346, 2, 1, '111', '222', 1, 2, 333, 13.32, 0, '2025-11-03', 1, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_date`, `flag`, `remark`) VALUES (347, 1, 1, '111', '222', 2, 2, 333, 19.98, 0, '2025-11-03', 1, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_date`, `flag`, `remark`) VALUES (348, 1, 1, '111', '222', 2, 2, 333, 19.98, 0, '2025-11-03', 1, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_date`, `flag`, `remark`) VALUES (349, 2, 1, '111', '222', 1, 2, 333, 13.32, 0, '2025-11-03', 1, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_date`, `flag`, `remark`) VALUES (350, 2, 1, '111', '222', 1, 1, 333, 13.32, 0, '2025-11-03', 1, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_date`, `flag`, `remark`) VALUES (351, 1, 1, '111', '222', 2, 1, 333, 13.32, 0, '2025-11-03', 1, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_date`, `flag`, `remark`) VALUES (352, 1, 1, '111', '222', 2, 1, 333, 13.32, 0, '2025-11-03', 1, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_date`, `flag`, `remark`) VALUES (353, 2, 1, '111', '222', 1, 1, 333, 13.32, 0, '2025-11-03', 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_date`, `flag`, `remark`) VALUES (354, 3, 1, '111', '222', 1, 1, 333, 13.32, 0, '2025-11-03', 1, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_date`, `flag`, `remark`) VALUES (355, 4, 1, '111', '222', 1, 1, 333, 13.32, 0, '2025-11-03', 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_date`, `flag`, `remark`) VALUES (356, 1, 1, '111', '222', 2, 1, 333, 13.32, 0, '2025-11-03', 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_date`, `flag`, `remark`) VALUES (357, 5, 1, '111', '222', 2, 1, 333, 13.32, 0, '2025-11-03', 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_date`, `flag`, `remark`) VALUES (358, 6, 1, '111', '222', 1, 1, 333, 13.32, 0, '2025-11-03', 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_date`, `flag`, `remark`) VALUES (359, 7, 1, '111', '222', 2, 1, 333, 13.32, 0, '2025-11-03', 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_date`, `flag`, `remark`) VALUES (360, 8, 1, '111', '222', 2, 1, 333, 13.32, 0, '2025-11-03', 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_date`, `flag`, `remark`) VALUES (361, 1, 1, '222', '333', 2, 1, 4444, 177.76, 0, '2025-11-03', 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_date`, `flag`, `remark`) VALUES (362, 4, 1, '232', '444', 1, 1, 222, 8.88, 0, '2025-11-03', 0, NULL);
INSERT INTO `job` (`id`, `employee_id`, `factory_id`, `number`, `style_number`, `mode_id`, `category_id`, `quantity`, `salary`, `version`, `created_date`, `flag`, `remark`) VALUES (363, 1, 3, '222', '222', 2, 1, 222, 8.88, 0, '2025-11-03', 0, NULL);
COMMIT;

-- ----------------------------
-- Table structure for job_category
-- ----------------------------
DROP TABLE IF EXISTS `job_category`;
CREATE TABLE `job_category` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `category` varchar(32) NOT NULL COMMENT '工作类型，例如 小花、大花、裤页',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `created_date` date NOT NULL COMMENT '创建日期',
  `flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除状态 0否 1是',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作类型表';

-- ----------------------------
-- Records of job_category
-- ----------------------------
BEGIN;
INSERT INTO `job_category` (`id`, `category`, `version`, `created_date`, `flag`, `remark`) VALUES (1, '小花', 0, '2025-10-03', 0, NULL);
INSERT INTO `job_category` (`id`, `category`, `version`, `created_date`, `flag`, `remark`) VALUES (2, '大花', 0, '2025-10-03', 0, NULL);
INSERT INTO `job_category` (`id`, `category`, `version`, `created_date`, `flag`, `remark`) VALUES (3, '裤页', 0, '2025-10-03', 0, NULL);
COMMIT;

-- ----------------------------
-- Table structure for job_mode
-- ----------------------------
DROP TABLE IF EXISTS `job_mode`;
CREATE TABLE `job_mode` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `mode` varchar(32) NOT NULL COMMENT '工作方式，例如 1压花、2刮胶 3xxx',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `created_date` date NOT NULL COMMENT '创建日期',
  `flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除状态 0否 1是',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作方式表';

-- ----------------------------
-- Records of job_mode
-- ----------------------------
BEGIN;
INSERT INTO `job_mode` (`id`, `mode`, `version`, `created_date`, `flag`, `remark`) VALUES (1, '压花', 0, '2025-10-04', 0, NULL);
INSERT INTO `job_mode` (`id`, `mode`, `version`, `created_date`, `flag`, `remark`) VALUES (2, '刮胶', 0, '2025-10-04', 0, NULL);
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
  `created_date` date NOT NULL COMMENT '创建日期',
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
INSERT INTO `job_quotation` (`id`, `mode_id`, `category_id`, `quotation`, `version`, `created_date`, `flag`, `remark`) VALUES (1, 1, 1, 0.04, 0, '2025-10-16', 0, NULL);
INSERT INTO `job_quotation` (`id`, `mode_id`, `category_id`, `quotation`, `version`, `created_date`, `flag`, `remark`) VALUES (2, 1, 2, 0.04, 0, '2025-10-16', 0, NULL);
INSERT INTO `job_quotation` (`id`, `mode_id`, `category_id`, `quotation`, `version`, `created_date`, `flag`, `remark`) VALUES (3, 1, 3, 0.08, 0, '2025-10-16', 0, NULL);
INSERT INTO `job_quotation` (`id`, `mode_id`, `category_id`, `quotation`, `version`, `created_date`, `flag`, `remark`) VALUES (4, 2, 1, 0.04, 0, '2025-10-16', 0, NULL);
INSERT INTO `job_quotation` (`id`, `mode_id`, `category_id`, `quotation`, `version`, `created_date`, `flag`, `remark`) VALUES (5, 2, 2, 0.06, 0, '2025-10-16', 0, NULL);
INSERT INTO `job_quotation` (`id`, `mode_id`, `category_id`, `quotation`, `version`, `created_date`, `flag`, `remark`) VALUES (6, 2, 3, 0.10, 0, '2025-10-16', 0, NULL);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
