CREATE TABLE IF NOT EXISTS `transfer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `group_id` bigint DEFAULT NULL,
  `member_id` varchar(255) DEFAULT NULL,
  `amount` decimal(38,0) DEFAULT NULL,
  `transfer_date` date DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
