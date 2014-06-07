CREATE DATABASE  IF NOT EXISTS `aegis_information_schema` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `aegis_information_schema`;
-- MySQL dump 10.13  Distrib 5.6.13, for osx10.6 (i386)
--
-- Host: localhost    Database: aegis_information_schema
-- ------------------------------------------------------
-- Server version	5.6.17

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `TEST_DEMO_ORDERS`
--

DROP TABLE IF EXISTS `TEST_DEMO_ORDERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TEST_DEMO_ORDERS` (
  `_ID_` int(11) NOT NULL AUTO_INCREMENT,
  `_GROUP_` varchar(64) NOT NULL DEFAULT '%',
  `_USER_` varchar(128) NOT NULL DEFAULT '%',
  `_HOST_` varchar(64) NOT NULL DEFAULT '%',
  `_DEVICE_` varchar(64) NOT NULL DEFAULT '%',
  `_ACCESSOR_` varchar(64) NOT NULL,
  `_TABLE_ACCESS_` set('VALIDATE','INSERT','SELECT','UPDATE','DELETE','DROP','TRUNCATE','CREATE','LOAD','BLOCKED') NOT NULL DEFAULT '',
  `ID` set('VALIDATE','INSERT','SELECT','UPDATE','BLOCKED') NOT NULL DEFAULT '',
  `ACCOUNT_ID` set('VALIDATE','INSERT','SELECT','UPDATE','BLOCKED') NOT NULL DEFAULT '',
  `DESCRIPTION` set('VALIDATE','INSERT','SELECT','UPDATE','BLOCKED') NOT NULL DEFAULT '',
  `ORDER_RECEIVED` set('VALIDATE','INSERT','SELECT','UPDATE','BLOCKED') NOT NULL DEFAULT '',
  `ORDER_PROCESSED` set('VALIDATE','INSERT','SELECT','UPDATE','BLOCKED') NOT NULL DEFAULT '',
  `ORDER_STATUS_ID` set('VALIDATE','INSERT','SELECT','UPDATE','BLOCKED') NOT NULL DEFAULT '',
  `ACCESSOR_ID` set('INSERT','SELECT','BLOCKED') NOT NULL DEFAULT '',
  PRIMARY KEY (`_ID_`),
  UNIQUE KEY `ACCESSOR_IDX` (`_GROUP_`,`_USER_`,`_HOST_`,`_DEVICE_`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `TEST_DEMO_ORDERS`
--

LOCK TABLES `TEST_DEMO_ORDERS` WRITE;
/*!40000 ALTER TABLE `TEST_DEMO_ORDERS` DISABLE KEYS */;
INSERT INTO `TEST_DEMO_ORDERS` VALUES (1,'%','%','%','%','%','','','','','','','',''),(17,'USER','nikita','%','%','ACCESSOR_ID','INSERT,SELECT,UPDATE,DELETE','SELECT','INSERT,SELECT,UPDATE','INSERT,SELECT,UPDATE','INSERT,SELECT,UPDATE','INSERT,SELECT,UPDATE','INSERT,SELECT,UPDATE',''),(18,'%','nikita','%','%','ACCESSOR_ID','INSERT,SELECT,UPDATE,DELETE','SELECT','INSERT,SELECT,UPDATE','INSERT,SELECT,UPDATE','INSERT,SELECT,UPDATE','INSERT,SELECT,UPDATE','INSERT,SELECT,UPDATE',''),(19,'%','mike','%','%','ACCESSOR_ID','INSERT,SELECT,UPDATE,DELETE','SELECT','INSERT,SELECT,UPDATE','INSERT,SELECT,UPDATE','INSERT,SELECT,UPDATE','INSERT,SELECT,UPDATE','INSERT,SELECT,UPDATE','INSERT,SELECT');
/*!40000 ALTER TABLE `TEST_DEMO_ORDERS` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-04-21 21:35:06