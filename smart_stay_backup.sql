-- MySQL dump 10.13  Distrib 9.5.0, for macos15 (arm64)
--
-- Host: localhost    Database: smart_stay
-- ------------------------------------------------------
-- Server version	9.5.0

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
-- Table structure for table `admins`
--

DROP TABLE IF EXISTS `admins`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admins` (
  `is_super_admin` bit(1) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `FKgc8dtql9mkq268detxiox7fpm` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admins`
--

LOCK TABLES `admins` WRITE;
/*!40000 ALTER TABLE `admins` DISABLE KEYS */;
INSERT INTO `admins` VALUES (_binary '',1);
/*!40000 ALTER TABLE `admins` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_messages`
--

DROP TABLE IF EXISTS `chat_messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_messages` (
  `is_deleted` bit(1) NOT NULL,
  `is_read` bit(1) NOT NULL,
  `chat_room_id` bigint NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `read_at` datetime(6) DEFAULT NULL,
  `sender_customer_id` bigint DEFAULT NULL,
  `sender_user_id` bigint DEFAULT NULL,
  `sent_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `content` varchar(255) NOT NULL,
  `file_name` varchar(255) DEFAULT NULL,
  `file_url` varchar(255) DEFAULT NULL,
  `message_type` varchar(255) NOT NULL,
  `sender_type` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbcsxusjp1v4rd8879fhvq8ssb` (`chat_room_id`),
  KEY `FKt1mcw0ee39s5hk6ggeyo4kbkb` (`sender_customer_id`),
  KEY `FKg9d1odxgyj8y7in19vun7txwq` (`sender_user_id`),
  CONSTRAINT `FKbcsxusjp1v4rd8879fhvq8ssb` FOREIGN KEY (`chat_room_id`) REFERENCES `chat_rooms` (`id`),
  CONSTRAINT `FKg9d1odxgyj8y7in19vun7txwq` FOREIGN KEY (`sender_user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKt1mcw0ee39s5hk6ggeyo4kbkb` FOREIGN KEY (`sender_customer_id`) REFERENCES `customers` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_messages`
--

LOCK TABLES `chat_messages` WRITE;
/*!40000 ALTER TABLE `chat_messages` DISABLE KEYS */;
/*!40000 ALTER TABLE `chat_messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_rooms`
--

DROP TABLE IF EXISTS `chat_rooms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_rooms` (
  `is_active` bit(1) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `customer_id` bigint NOT NULL,
  `customer_unread_count` bigint NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `landlord_id` bigint NOT NULL,
  `last_message_at` datetime(6) DEFAULT NULL,
  `room_post_id` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_unread_count` bigint NOT NULL,
  `last_message` varchar(255) DEFAULT NULL,
  `room_key` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKavfwohb5d9ekrwhha494alydi` (`room_key`),
  KEY `FKg7i16vb314i2vhhj273ve36c4` (`customer_id`),
  KEY `FKh8fo6t4p5k10l0i4h4hecrqit` (`landlord_id`),
  CONSTRAINT `FKg7i16vb314i2vhhj273ve36c4` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`user_id`),
  CONSTRAINT `FKh8fo6t4p5k10l0i4h4hecrqit` FOREIGN KEY (`landlord_id`) REFERENCES `landlords` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_rooms`
--

LOCK TABLES `chat_rooms` WRITE;
/*!40000 ALTER TABLE `chat_rooms` DISABLE KEYS */;
INSERT INTO `chat_rooms` VALUES (_binary '','2025-11-27 16:18:12.000000',4,0,1,2,'2026-03-16 16:18:12.000000',1,'2026-03-17 16:18:12.000000',1,'Anh ơi em đã chuyển khoản tiền thuê tháng 12 rồi ạ','CR_4_2'),(_binary '','2025-12-02 16:18:12.000000',5,0,2,2,'2026-03-17 13:18:12.000000',2,'2026-03-17 16:18:12.000000',1,'Tháng này hóa đơn điện cao hơn tháng trước nhiều vậy anh?','CR_5_2'),(_binary '','2025-12-07 16:18:12.000000',6,1,3,3,'2026-03-15 16:18:12.000000',4,'2026-03-17 16:18:12.000000',0,'Ok em, chị sẽ kiểm tra và phản hồi sớm nhé','CR_6_3'),(_binary '','2025-12-12 16:18:12.000000',7,0,4,3,'2026-03-17 11:18:12.000000',5,'2026-03-17 16:18:12.000000',2,'Chị ơi phòng em bị hỏng vòi nước ạ','CR_7_3'),(_binary '','2026-03-12 16:18:12.000000',4,0,5,2,'2026-03-16 16:18:12.000000',3,'2026-03-17 16:18:12.000000',1,'Anh ơi phòng P103 còn trống không ạ?','CR_4_2_p103');
/*!40000 ALTER TABLE `chat_rooms` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contracts`
--

DROP TABLE IF EXISTS `contracts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contracts` (
  `actual_end_date` date DEFAULT NULL,
  `deposit_amount` double DEFAULT NULL,
  `deposit_returned` bit(1) NOT NULL,
  `end_date` date DEFAULT NULL,
  `monthly_rent` double NOT NULL,
  `start_date` date NOT NULL,
  `billing_date` bigint NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `customer_id` bigint NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `landlord_id` bigint NOT NULL,
  `num_occupants` bigint NOT NULL,
  `room_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `contract_code` varchar(255) NOT NULL,
  `contract_file_url` varchar(255) DEFAULT NULL,
  `notes` varchar(255) DEFAULT NULL,
  `status` varchar(255) NOT NULL,
  `termination_reason` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKcumggxjbttojw3k8b7fgleib6` (`contract_code`),
  KEY `FKgcu7bfqv1j7nltm5uhk91kxcy` (`customer_id`),
  KEY `FKqceqp2l4dkoca36379b9ec6hq` (`landlord_id`),
  KEY `FKju1b0xobla9t8oexrb8lpi8jq` (`room_id`),
  CONSTRAINT `FKgcu7bfqv1j7nltm5uhk91kxcy` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`user_id`),
  CONSTRAINT `FKju1b0xobla9t8oexrb8lpi8jq` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`),
  CONSTRAINT `FKqceqp2l4dkoca36379b9ec6hq` FOREIGN KEY (`landlord_id`) REFERENCES `landlords` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contracts`
--

LOCK TABLES `contracts` WRITE;
/*!40000 ALTER TABLE `contracts` DISABLE KEYS */;
INSERT INTO `contracts` VALUES (NULL,7000000,_binary '\0','2024-12-31',3500000,'2024-01-01',5,'2025-11-27 16:17:27.000000',4,1,2,1,1,'2026-03-17 16:17:27.000000','HD-2024-001',NULL,NULL,'ACTIVE',NULL),(NULL,8000000,_binary '\0','2025-02-28',4000000,'2024-03-01',5,'2025-12-02 16:17:27.000000',5,2,2,2,2,'2026-03-17 16:17:27.000000','HD-2024-002',NULL,NULL,'ACTIVE',NULL),(NULL,9000000,_binary '\0','2025-03-31',4500000,'2024-04-01',5,'2025-12-07 16:17:27.000000',6,3,3,2,4,'2026-03-17 16:17:27.000000','HD-2024-003',NULL,NULL,'ACTIVE',NULL),(NULL,11000000,_binary '\0','2025-05-31',5500000,'2024-06-01',5,'2025-12-12 16:17:27.000000',7,4,3,2,5,'2026-03-17 16:17:27.000000','HD-2024-004',NULL,NULL,'ACTIVE',NULL),('2023-12-31',6400000,_binary '','2023-12-31',3200000,'2023-01-01',5,'2025-02-10 16:17:27.000000',6,5,2,1,1,'2026-03-17 16:17:27.000000','HD-2023-005',NULL,'Khách không gia hạn','EXPIRED',NULL),('2024-05-31',9000000,_binary '\0','2024-12-31',4500000,'2024-01-01',5,'2025-08-29 16:17:27.000000',4,6,3,2,4,'2026-03-17 16:17:27.000000','HD-2024-006',NULL,NULL,'TERMINATED','Khách chuyển công tác ra Hà Nội');
/*!40000 ALTER TABLE `contracts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customers`
--

DROP TABLE IF EXISTS `customers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customers` (
  `is_verified` bit(1) NOT NULL,
  `user_id` bigint NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `id_card_number` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `UKlebm5t751soii3dornwrx62n6` (`id_card_number`),
  CONSTRAINT `FKrh1g1a20omjmn6kurd35o3eit` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customers`
--

LOCK TABLES `customers` WRITE;
/*!40000 ALTER TABLE `customers` DISABLE KEYS */;
INSERT INTO `customers` VALUES (_binary '',4,'205 Lê Lai, Quận 1, TP.HCM','001199010001'),(_binary '',5,'38 Phạm Ngũ Lão, Quận 1, TP.HCM','001200010002'),(_binary '',6,'77 Bùi Thị Xuân, Quận 1, TP.HCM','001197010003'),(_binary '\0',7,'99 Nguyễn Thị Minh Khai, TP.HCM','001201010004');
/*!40000 ALTER TABLE `customers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `landlords`
--

DROP TABLE IF EXISTS `landlords`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `landlords` (
  `is_verified` bit(1) NOT NULL,
  `user_id` bigint NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `business_license_url` varchar(255) DEFAULT NULL,
  `id_card_number` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `UK2nb7d0b0w1can8a5wdam3a16k` (`id_card_number`),
  CONSTRAINT `FK1u2i33ahunry1ayfiw1rmdib8` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `landlords`
--

LOCK TABLES `landlords` WRITE;
/*!40000 ALTER TABLE `landlords` DISABLE KEYS */;
INSERT INTO `landlords` VALUES (_binary '',2,'12 Nguyễn Trãi, Quận 1, TP.HCM',NULL,'079175001234'),(_binary '\0',3,'45 Lê Lợi, Quận 3, TP.HCM',NULL,'079182005678');
/*!40000 ALTER TABLE `landlords` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `is_read` bit(1) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `read_at` datetime(6) DEFAULT NULL,
  `recipient_id` bigint NOT NULL,
  `reference_id` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `content` varchar(255) NOT NULL,
  `notification_type` varchar(255) NOT NULL,
  `recipient_type` varchar(255) NOT NULL,
  `title` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2ohxtlrrsjkm5u8wc4ns4ti4h` (`recipient_id`),
  CONSTRAINT `FK2ohxtlrrsjkm5u8wc4ns4ti4h` FOREIGN KEY (`recipient_id`) REFERENCES `customers` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
INSERT INTO `notifications` VALUES (_binary '','2026-01-01 16:18:44.000000',1,'2026-01-02 16:18:44.000000',4,1,'2026-03-17 16:18:44.000000','Hóa đơn điện nước tháng 10/2024 phòng P101 đã được tạo. Tổng: 685,000đ. Hạn: 15/10/2024.','UTILITY_BILL','CUSTOMER','Hóa đơn tháng 10/2024 đã phát sinh'),(_binary '','2026-01-01 16:18:44.000000',2,'2026-01-04 16:18:44.000000',4,1,'2026-03-17 16:18:44.000000','Bạn đã thanh toán tiền thuê + điện nước tháng 10/2024. Tổng: 4,185,000đ.','PAYMENT','CUSTOMER','Thanh toán tháng 10/2024 thành công'),(_binary '','2026-01-31 16:18:44.000000',3,'2026-02-02 16:18:44.000000',4,2,'2026-03-17 16:18:44.000000','Hóa đơn điện nước tháng 11/2024 phòng P101 đã được tạo. Tổng: 702,500đ. Hạn: 15/11/2024.','UTILITY_BILL','CUSTOMER','Hóa đơn tháng 11/2024 đã phát sinh'),(_binary '\0','2026-03-14 16:18:44.000000',4,NULL,4,3,'2026-03-17 16:18:44.000000','Bạn chưa thanh toán tiền thuê tháng 12/2024. Hạn chót: 15/12/2024.','PAYMENT_REMIND','CUSTOMER','Nhắc nhở: Chưa thanh toán tháng 12/2024'),(_binary '\0','2026-03-07 16:18:44.000000',5,NULL,4,1,'2026-03-17 16:18:44.000000','Hợp đồng HD-2024-001 sẽ hết hạn vào 31/12/2024. Vui lòng liên hệ chủ nhà để gia hạn.','CONTRACT','CUSTOMER','Hợp đồng sắp hết hạn'),(_binary '','2026-01-31 16:18:44.000000',6,'2026-02-02 16:18:44.000000',5,4,'2026-03-17 16:18:44.000000','Hóa đơn điện nước tháng 11/2024 phòng P102 đã được tạo. Tổng: 735,000đ. Hạn: 15/11/2024.','UTILITY_BILL','CUSTOMER','Hóa đơn tháng 11/2024 đã phát sinh'),(_binary '\0','2026-03-14 16:18:44.000000',7,NULL,5,5,'2026-03-17 16:18:44.000000','Bạn chưa thanh toán tiền thuê tháng 12/2024. Hạn chót: 15/12/2024.','PAYMENT_REMIND','CUSTOMER','Nhắc nhở: Chưa thanh toán tháng 12/2024'),(_binary '','2026-01-31 16:18:44.000000',8,'2026-02-02 16:18:44.000000',6,6,'2026-03-17 16:18:44.000000','Hóa đơn điện nước tháng 11/2024 phòng A01 đã được tạo. Tổng: 820,400đ. Hạn: 15/11/2024.','UTILITY_BILL','CUSTOMER','Hóa đơn tháng 11/2024 đã phát sinh'),(_binary '\0','2026-03-14 16:18:44.000000',9,NULL,6,7,'2026-03-17 16:18:44.000000','Bạn chưa thanh toán tiền thuê tháng 12/2024. Hạn chót: 15/12/2024.','PAYMENT_REMIND','CUSTOMER','Nhắc nhở: Chưa thanh toán tháng 12/2024'),(_binary '','2026-01-31 16:18:44.000000',10,'2026-02-02 16:18:44.000000',7,8,'2026-03-17 16:18:44.000000','Hóa đơn điện nước tháng 11/2024 phòng A02 đã được tạo. Tổng: 883,600đ. Hạn: 15/11/2024.','UTILITY_BILL','CUSTOMER','Hóa đơn tháng 11/2024 đã phát sinh'),(_binary '\0','2026-03-15 16:18:44.000000',11,NULL,7,9,'2026-03-17 16:18:44.000000','Bạn chưa thanh toán tiền thuê tháng 12/2024, đã quá hạn 2 ngày. Phí trễ hạn: 50,000đ.','PAYMENT_OVERDUE','CUSTOMER','Cảnh báo: Thanh toán quá hạn');
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rent_payments`
--

DROP TABLE IF EXISTS `rent_payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rent_payments` (
  `due_date` date DEFAULT NULL,
  `late_fee` double NOT NULL,
  `paid_date` date DEFAULT NULL,
  `rent_amount` double NOT NULL,
  `total_amount` double NOT NULL,
  `utility_amount` double NOT NULL,
  `contract_id` bigint NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `utility_bill_id` bigint DEFAULT NULL,
  `billing_month` varchar(255) NOT NULL,
  `id_card_number` varchar(255) DEFAULT NULL,
  `notes` varchar(255) DEFAULT NULL,
  `payment_method` varchar(255) DEFAULT NULL,
  `receipt_url` varchar(255) DEFAULT NULL,
  `status` varchar(255) NOT NULL,
  `transaction_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK4y6lu3o00rkh2t6vs11q4bpf5` (`utility_bill_id`),
  KEY `FKbqvdpu86rmlxnjllf65ncyqbq` (`contract_id`),
  CONSTRAINT `FK9fp7shcgwdy55s6iocjqa9oqt` FOREIGN KEY (`utility_bill_id`) REFERENCES `utility_bills` (`id`),
  CONSTRAINT `FKbqvdpu86rmlxnjllf65ncyqbq` FOREIGN KEY (`contract_id`) REFERENCES `contracts` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rent_payments`
--

LOCK TABLES `rent_payments` WRITE;
/*!40000 ALTER TABLE `rent_payments` DISABLE KEYS */;
INSERT INTO `rent_payments` VALUES ('2024-10-15',0,'2024-10-10',3500000,4185000,685000,1,'2026-01-01 16:17:58.000000',1,'2026-03-17 16:17:58.000000',1,'2024-10','001199010001',NULL,'BANK_TRANSFER',NULL,'PAID','TXN20241001'),('2024-11-15',0,'2024-11-08',3500000,4202500,702500,1,'2026-01-31 16:17:58.000000',2,'2026-03-17 16:17:58.000000',2,'2024-11','001199010001',NULL,'MOMO',NULL,'PAID','MOMO20241101'),('2024-12-15',0,NULL,3500000,4202500,702500,1,'2026-03-02 16:17:58.000000',3,'2026-03-17 16:17:58.000000',3,'2024-12','001199010001',NULL,NULL,NULL,'UNPAID',NULL),('2024-11-15',0,'2024-11-12',4000000,4735000,735000,2,'2026-01-31 16:17:58.000000',4,'2026-03-17 16:17:58.000000',4,'2024-11','001200010002',NULL,'BANK_TRANSFER',NULL,'PAID','TXN20241102'),('2024-12-15',0,NULL,4000000,4752500,752500,2,'2026-03-02 16:17:58.000000',5,'2026-03-17 16:17:58.000000',5,'2024-12','001200010002',NULL,NULL,NULL,'UNPAID',NULL),('2024-11-15',0,'2024-11-07',4500000,5320400,820400,3,'2026-01-31 16:17:58.000000',6,'2026-03-17 16:17:58.000000',6,'2024-11','001197010003',NULL,'CASH',NULL,'PAID',NULL),('2024-12-15',0,NULL,4500000,5316600,816600,3,'2026-03-02 16:17:58.000000',7,'2026-03-17 16:17:58.000000',7,'2024-12','001197010003',NULL,NULL,NULL,'UNPAID',NULL),('2024-11-15',0,'2024-11-10',5500000,6383600,883600,4,'2026-01-31 16:17:58.000000',8,'2026-03-17 16:17:58.000000',8,'2024-11','001201010004',NULL,'BANK_TRANSFER',NULL,'PAID','TXN20241104'),('2024-12-15',50000,NULL,5500000,6456400,906400,4,'2026-03-02 16:17:58.000000',9,'2026-03-17 16:17:58.000000',9,'2024-12','001201010004','Quá hạn 2 ngày, phí trễ hạn 50,000đ',NULL,NULL,'UNPAID',NULL);
/*!40000 ALTER TABLE `rent_payments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rooms`
--

DROP TABLE IF EXISTS `rooms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rooms` (
  `area_m2` double DEFAULT NULL,
  `cleaning_fee` double NOT NULL,
  `electricity_price_per_kwh` decimal(38,2) NOT NULL,
  `internet_fee` double NOT NULL,
  `parking_fee` double NOT NULL,
  `rent_price` decimal(38,2) NOT NULL,
  `water_price_per_m3` decimal(38,2) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `landlord_id` bigint NOT NULL,
  `max_occupants` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `address` varchar(255) NOT NULL,
  `city` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `district` varchar(255) DEFAULT NULL,
  `room_number` varchar(255) NOT NULL,
  `room_type` varchar(255) DEFAULT NULL,
  `status` varchar(255) NOT NULL,
  `ward` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKld1trswv362ilgdpw4l8sn39t` (`landlord_id`),
  CONSTRAINT `FKld1trswv362ilgdpw4l8sn39t` FOREIGN KEY (`landlord_id`) REFERENCES `landlords` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rooms`
--

LOCK TABLES `rooms` WRITE;
/*!40000 ALTER TABLE `rooms` DISABLE KEYS */;
INSERT INTO `rooms` VALUES (25,50000,3500.00,100000,150000,3500000.00,15000.00,'2025-09-28 16:17:19.000000',1,2,2,'2026-03-17 16:17:19.000000','12 Nguyễn Trãi','TP.HCM','Phòng đơn thoáng mát, có ban công, gần trung tâm','Quận 1','P101','SINGLE','RENTED','Phường Nguyễn Cư Trinh'),(30,50000,3500.00,100000,150000,4000000.00,15000.00,'2025-10-03 16:17:19.000000',2,2,2,'2026-03-17 16:17:19.000000','12 Nguyễn Trãi','TP.HCM','Phòng đôi rộng rãi, đầy đủ tiện nghi','Quận 1','P102','DOUBLE','RENTED','Phường Nguyễn Cư Trinh'),(20,50000,3500.00,100000,0,3000000.00,15000.00,'2025-10-08 16:17:19.000000',3,2,1,'2026-03-17 16:17:19.000000','12 Nguyễn Trãi','TP.HCM','Phòng nhỏ gọn, phù hợp 1 người, có gác lửng','Quận 1','P103','SINGLE','AVAILABLE','Phường Nguyễn Cư Trinh'),(28,60000,3800.00,120000,180000,4500000.00,18000.00,'2025-10-13 16:17:19.000000',4,3,2,'2026-03-17 16:17:19.000000','45 Lê Lợi','TP.HCM','Phòng mới xây, nội thất hiện đại, view đẹp','Quận 3','A01','DOUBLE','RENTED','Phường Bến Nghé'),(32,60000,3800.00,150000,180000,5500000.00,18000.00,'2025-10-18 16:17:19.000000',5,3,3,'2026-03-17 16:17:19.000000','45 Lê Lợi','TP.HCM','Studio cao cấp, có bếp và WC riêng','Quận 3','A02','STUDIO','RENTED','Phường Bến Nghé'),(22,60000,3800.00,120000,0,3800000.00,18000.00,'2025-10-23 16:17:19.000000',6,3,1,'2026-03-17 16:17:19.000000','45 Lê Lợi','TP.HCM','Phòng đơn cho sinh viên, yên tĩnh','Quận 3','A03','SINGLE','AVAILABLE','Phường Bến Nghé');
/*!40000 ALTER TABLE `rooms` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `date_of_birth` date DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_type` varchar(31) NOT NULL,
  `avatar_url` varchar(255) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `full_name` varchar(255) NOT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  `username` varchar(255) NOT NULL,
  `role` enum('ADMIN','CUSTOMER','LANDLORD') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (NULL,_binary '','2026-03-17 16:10:54.530197',1,NULL,'USER',NULL,'admin@rentapp.vn','Nguyễn Văn Admin',NULL,'$2a$10$cY8X/SzHH82H0/YJbi5LrOC9N.enwcueSEsb66KGKq8uH3TLt5YLq','0234234243','admin','CUSTOMER'),(NULL,_binary '','2026-03-17 16:11:20.189633',2,NULL,'USER',NULL,'khoa.tran@gmail.com','Trần Minh Khoa',NULL,'$2a$10$3ewDMI3IIZwbU7JPIVH9guVX4n1lig2Ydc9B2DEqVvtebZpVd87ga','0123423543','khoatran','LANDLORD'),(NULL,_binary '','2026-03-17 16:11:43.258421',3,NULL,'USER',NULL,'mai.le@gmail.com','Lê Thị Mai',NULL,'$2a$10$VO5hnndsSsJCBbi.6KoMm.T3kvmp3Sm2glZ1QZeGqVfaPTIDkWHte','0834223423','maile','LANDLORD'),(NULL,_binary '','2026-03-17 16:12:03.919039',4,NULL,'USER',NULL,'tu.nguyen@gmail.com','Nguyễn Văn Tú',NULL,'$2a$10$9zon/p4TREMcyKUwbl/SIOp2HzY7V52q.2UZTPsS2d3phXBKmzsGC','023484845','tunguyen','CUSTOMER'),(NULL,_binary '','2026-03-17 16:12:21.910550',5,NULL,'USER',NULL,'huong.tran@gmail.com','Trần Thị Hương',NULL,'$2a$10$k9fJI7Z0aLa5MvvKxcHlkOWXRDUvQ2Zi6vRFaVSvlJmzOc2bXbfLq','0234823412','huongtran','CUSTOMER'),(NULL,_binary '','2026-03-17 16:12:42.731987',6,NULL,'USER',NULL,'cuong.le@gmail.com','Lê Văn Cường',NULL,'$2a$10$GBuruEpAD59CcvrtxmhueOHG9hbyN6cJTpH00OFl5/cUko0cqZF6G','08546734859','cuongle','CUSTOMER'),(NULL,_binary '','2026-03-17 16:13:01.696457',7,NULL,'USER',NULL,'thuy.pham@gmail.com','Phạm Thị Thủy',NULL,'$2a$10$bTlHrQNrkJpMvMF9xuAaneGDENboeI02/Z8aXyxRhRVjZmJwNAT/.','08345734823','thuypham','CUSTOMER');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `utility_bills`
--

DROP TABLE IF EXISTS `utility_bills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `utility_bills` (
  `cleaning_fee` double NOT NULL,
  `electricity_amount` decimal(38,2) DEFAULT NULL,
  `electricity_consumed` double DEFAULT NULL,
  `electricity_new_index` double NOT NULL,
  `electricity_old_index` double NOT NULL,
  `electricity_price_per_kwh` decimal(38,2) DEFAULT NULL,
  `internet_fee` double NOT NULL,
  `other_fee` double NOT NULL,
  `paid_at` date DEFAULT NULL,
  `parking_fee` double NOT NULL,
  `status` tinyint NOT NULL,
  `total_amount` decimal(38,2) NOT NULL,
  `water_amount` decimal(38,2) DEFAULT NULL,
  `water_consumed` double DEFAULT NULL,
  `water_new_index` double NOT NULL,
  `water_old_index` double NOT NULL,
  `water_price_per_m3` double DEFAULT NULL,
  `billing_month` bigint NOT NULL,
  `contract_id` bigint DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `due_date` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `notes` varchar(255) DEFAULT NULL,
  `other_fee_note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKcydc8kjrwm8hikv4k8mm1qeux` (`contract_id`),
  KEY `FK1kumd4cy1h5np2ugslb6t3r68` (`room_id`),
  KEY `FK110gran33gc1nbdj97224f6v` (`user_id`),
  CONSTRAINT `FK110gran33gc1nbdj97224f6v` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FK1kumd4cy1h5np2ugslb6t3r68` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`),
  CONSTRAINT `FKcydc8kjrwm8hikv4k8mm1qeux` FOREIGN KEY (`contract_id`) REFERENCES `contracts` (`id`),
  CONSTRAINT `utility_bills_chk_1` CHECK ((`status` between 0 and 1))
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `utility_bills`
--

LOCK TABLES `utility_bills` WRITE;
/*!40000 ALTER TABLE `utility_bills` DISABLE KEYS */;
INSERT INTO `utility_bills` VALUES (50000,280000.00,80,1080,1000,3500.00,100000,0,'2024-10-10',150000,1,685000.00,105000.00,7,47,40,15000,202410,1,'2026-01-01 16:17:45.000000','2026-01-31 16:17:45.000000',1,1,'2026-03-17 16:17:45.000000',2,NULL,NULL),(50000,297500.00,85,1165,1080,3500.00,100000,0,'2024-11-08',150000,1,702500.00,105000.00,7,54,47,15000,202411,1,'2026-01-31 16:17:45.000000','2026-03-02 16:17:45.000000',2,1,'2026-03-17 16:17:45.000000',2,NULL,NULL),(50000,297500.00,85,1250,1165,3500.00,100000,0,NULL,150000,0,702500.00,105000.00,7,61,54,15000,202412,1,'2026-03-02 16:17:45.000000','2026-03-22 16:17:45.000000',3,1,'2026-03-17 16:17:45.000000',2,NULL,NULL),(50000,315000.00,90,2090,2000,3500.00,100000,0,'2024-11-12',150000,1,735000.00,120000.00,8,83,75,15000,202411,2,'2026-01-31 16:17:45.000000','2026-03-02 16:17:45.000000',4,2,'2026-03-17 16:17:45.000000',2,NULL,NULL),(50000,332500.00,95,2185,2090,3500.00,100000,0,NULL,150000,0,752500.00,120000.00,8,91,83,15000,202412,2,'2026-03-02 16:17:45.000000','2026-03-22 16:17:45.000000',5,2,'2026-03-17 16:17:45.000000',2,NULL,NULL),(60000,334400.00,88,588,500,3800.00,120000,0,'2024-11-07',180000,1,820400.00,126000.00,7,27,20,18000,202411,3,'2026-01-31 16:17:45.000000','2026-03-02 16:17:45.000000',6,4,'2026-03-17 16:17:45.000000',3,NULL,NULL),(60000,330600.00,87,675,588,3800.00,120000,0,NULL,180000,0,816600.00,126000.00,7,34,27,18000,202412,3,'2026-03-02 16:17:45.000000','2026-03-22 16:17:45.000000',7,4,'2026-03-17 16:17:45.000000',3,NULL,NULL),(60000,349600.00,92,892,800,3800.00,150000,0,'2024-11-10',180000,1,883600.00,144000.00,8,38,30,18000,202411,4,'2026-01-31 16:17:45.000000','2026-03-02 16:17:45.000000',8,5,'2026-03-17 16:17:45.000000',3,NULL,NULL),(60000,372400.00,98,990,892,3800.00,150000,0,NULL,180000,0,906400.00,144000.00,8,46,38,18000,202412,4,'2026-03-02 16:17:45.000000','2026-03-22 16:17:45.000000',9,5,'2026-03-17 16:17:45.000000',3,'Quá hạn 2 ngày',NULL);
/*!40000 ALTER TABLE `utility_bills` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-17 16:35:25
