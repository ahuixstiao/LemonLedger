-- LemonLedger 数据库结构（只有表结构，没有任何业务数据）
--
-- 用途：新机器 / 新库初始化。导入后是一套空表，再从 deploy/migrate-*.sql 按顺序跑增量。
-- 生成方式：mysqldump --no-data --single-transaction --routines --triggers --default-character-set=utf8mb4 ledger
-- 注意：已剔除线上做字典合并时留下的那张临时备份表（它不属于项目结构）。
--
-- 导入：mysql -uroot -p ledger < ledger.sql   （库要先建好：CREATE DATABASE ledger DEFAULT CHARSET utf8mb4;）

-- MySQL dump 10.13  Distrib 8.0.46, for Linux (x86_64)
--
-- Host: localhost    Database: ledger
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `batch`
--

DROP TABLE IF EXISTS `batch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `batch` (
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
  UNIQUE KEY `unique_batch` (`factory_id`,`number`,`style_number`,`flag`),
  KEY `batch_number_index` (`number`)
) ENGINE=InnoDB AUTO_INCREMENT=14349 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='床表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `employee`
--

DROP TABLE IF EXISTS `employee`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `employee` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) NOT NULL COMMENT '员工姓名，用于查询工作内容',
  `mode_id` int DEFAULT NULL COMMENT '默认工作模式 1压花 2刮胶，可为空',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `created_date` date DEFAULT NULL COMMENT '创建时间',
  `updated_date` date DEFAULT NULL COMMENT '最后修改日期',
  `flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除状态 0否 1是',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_name` (`name`,`flag`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `factory`
--

DROP TABLE IF EXISTS `factory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `factory` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `factory_name` varchar(32) NOT NULL COMMENT '成衣厂名称',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `created_date` date DEFAULT NULL COMMENT '创建时间',
  `updated_date` date DEFAULT NULL COMMENT '最后修改日期',
  `flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除状态 0否 1是',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_factory_name` (`factory_name`,`flag`)
) ENGINE=InnoDB AUTO_INCREMENT=105 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成衣厂表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `factory_bill`
--

DROP TABLE IF EXISTS `factory_bill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `factory_bill` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `factory_id` int NOT NULL COMMENT '成衣厂ID',
  `number` varchar(32) NOT NULL COMMENT '床号',
  `batch_id` int DEFAULT NULL COMMENT '床ID，关联 batch.id',
  `piece_rate_id` int DEFAULT NULL COMMENT '报价ID，关联factory_quotation.id',
  `quantity` int NOT NULL COMMENT '数量',
  `bill` decimal(8,2) DEFAULT NULL COMMENT '本床账单',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `created_date` date NOT NULL COMMENT '创建时间',
  `updated_date` date DEFAULT NULL COMMENT '最后修改日期',
  `flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除状态 0否 1是',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `factory_bill_number_index` (`number`),
  KEY `idx_factory_bill_quotation_id` (`piece_rate_id`),
  KEY `factory_bill_factory_date_index` (`factory_id`,`created_date`),
  KEY `factory_bill_date_index` (`created_date`),
  KEY `factory_bill_batch_index` (`batch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4874 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成衣厂账单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `factory_piece_rate`
--

DROP TABLE IF EXISTS `factory_piece_rate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `factory_piece_rate` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `factory_id` int NOT NULL COMMENT '成衣厂ID',
  `style_number` varchar(32) DEFAULT NULL COMMENT '款式编号',
  `category_id` int NOT NULL COMMENT '工作类型 1小花、2大花、3裤页',
  `price` decimal(8,2) NOT NULL COMMENT '报价',
  `image_path` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '报价图片访问路径',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `created_date` date DEFAULT NULL COMMENT '创建时间',
  `updated_date` date DEFAULT NULL COMMENT '最后修改日期',
  `flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除状态 0否 1是',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=738 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成衣厂报价表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `job`
--

DROP TABLE IF EXISTS `job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `employee_id` int NOT NULL COMMENT '员工ID',
  `factory_id` int NOT NULL COMMENT '成衣厂ID',
  `number` varchar(32) NOT NULL COMMENT '床号',
  `style_number` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '款式编号 比如3101',
  `mode_id` int NOT NULL COMMENT '工作方式，例如 压花、刮胶',
  `category_id` int NOT NULL COMMENT '工作类型，例如 小花、大花、裤页',
  `work_order_id` bigint DEFAULT NULL COMMENT '工单ID',
  `batch_id` int DEFAULT NULL COMMENT '床ID，关联 batch.id',
  `quantity` int NOT NULL COMMENT '数量',
  `salary` decimal(8,2) NOT NULL COMMENT '本床薪资',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `created_date` date NOT NULL COMMENT '创建时间',
  `updated_date` date DEFAULT NULL COMMENT '最后修改日期',
  `flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除状态 0否 1是',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `job_category_id_index` (`category_id`),
  KEY `job_employee_id_index` (`employee_id`),
  KEY `job_factory_id_index` (`factory_id`),
  KEY `job_work_order_mode_index` (`work_order_id`,`mode_id`,`flag`),
  KEY `job_employee_date_index` (`employee_id`,`created_date`),
  KEY `job_batch_index` (`batch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18040 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作内容表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `job_category`
--

DROP TABLE IF EXISTS `job_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_category` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `category` varchar(32) NOT NULL COMMENT '工作类型，例如 小花、大花、裤页',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `created_date` date NOT NULL COMMENT '创建时间',
  `updated_date` date DEFAULT NULL COMMENT '最后修改日期',
  `flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除状态 0否 1是',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作类型表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `job_mode`
--

DROP TABLE IF EXISTS `job_mode`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_mode` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `mode` varchar(32) NOT NULL COMMENT '工作方式，例如 1压花、2刮胶 3xxx',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `created_date` date NOT NULL COMMENT '创建时间',
  `updated_date` date DEFAULT NULL COMMENT '最后修改日期',
  `flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除状态 0否 1是',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作方式表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `job_piece_rate`
--

DROP TABLE IF EXISTS `job_piece_rate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_piece_rate` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `mode_id` int NOT NULL COMMENT '工作方式',
  `category_id` int NOT NULL COMMENT '工作类型 1小花、2大花、3裤页',
  `price` decimal(8,2) NOT NULL COMMENT '报价',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `created_date` date NOT NULL COMMENT '创建时间',
  `updated_date` date DEFAULT NULL COMMENT '最后修改日期',
  `flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除状态 0否 1是',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `job_quotation_job_category_id_fk` (`category_id`),
  KEY `job_quotation_job_mode_id_fk` (`mode_id`),
  CONSTRAINT `job_quotation_job_category_id_fk` FOREIGN KEY (`category_id`) REFERENCES `job_category` (`id`) ON DELETE CASCADE,
  CONSTRAINT `job_quotation_job_mode_id_fk` FOREIGN KEY (`mode_id`) REFERENCES `job_mode` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作报价表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `job_work_order`
--

DROP TABLE IF EXISTS `job_work_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_work_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `factory_id` int NOT NULL COMMENT '成衣厂ID',
  `number` varchar(32) NOT NULL COMMENT '床号',
  `style_number` varchar(32) NOT NULL COMMENT '款式编号',
  `category_id` int NOT NULL COMMENT '工作类型，例如 小花、大花、裤页',
  `quantity` int NOT NULL COMMENT '数量',
  `batch_id` int DEFAULT NULL COMMENT '床ID，关联 batch.id',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `created_date` date NOT NULL COMMENT '创建时间',
  `updated_date` date DEFAULT NULL COMMENT '最后修改日期',
  `flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除状态 0否 1是',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `job_work_order_date_index` (`created_date`),
  KEY `job_work_order_factory_date_index` (`factory_id`,`created_date`),
  KEY `job_work_order_batch_index` (`batch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5849 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工作工单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'ledger'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed

