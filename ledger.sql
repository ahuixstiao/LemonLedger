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

 Date: 03/11/2025 02:24:10
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
  KEY `factory_bill_number_index` (`number`),
  KEY `factory_bill_style_number_index` (`style_number`)
) ENGINE=InnoDB AUTO_INCREMENT=128 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成衣厂账单表';

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
  KEY `job_category_id_index` (`category_id`),
  KEY `job_employee_id_index` (`employee_id`),
  KEY `job_factory_id_index` (`factory_id`)
) ENGINE=InnoDB AUTO_INCREMENT=356 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作内容表';

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

SET FOREIGN_KEY_CHECKS = 1;
