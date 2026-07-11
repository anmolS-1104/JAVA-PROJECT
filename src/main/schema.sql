-- 1. Create the Database Shell
CREATE DATABASE `complaints_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

-- 2. Tell the server to target this database
USE `complaints_db`;

-- 3. Create the Parent Table First (users)
CREATE TABLE `users` (
                         `id` int NOT NULL AUTO_INCREMENT,
                         `full_name` varchar(255) DEFAULT NULL,
                         `email` varchar(255) DEFAULT NULL,
                         `password` varchar(255) DEFAULT NULL,
                         `phone` varchar(50) DEFAULT NULL,
                         `role` varchar(20) DEFAULT 'CUSTOMER',
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 4. Create the Dependent Table Second (complaints)
CREATE TABLE `complaints` (
                              `id` int NOT NULL AUTO_INCREMENT,
                              `description` text,
                              `priority` varchar(50) DEFAULT NULL,
                              `department` varchar(50) DEFAULT NULL,
                              `status` varchar(50) DEFAULT NULL,
                              `user_id` int DEFAULT NULL,
                              `notes` text,
                              PRIMARY KEY (`id`),
                              KEY `fk_user` (`user_id`),
                              CONSTRAINT `fk_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;