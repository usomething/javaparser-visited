CREATE TABLE `autopart_method_call` (
  `id` int(64) NOT NULL AUTO_INCREMENT,
  `class_name` varchar(255) DEFAULT NULL,
  `method_name` varchar(255) DEFAULT NULL,
  `call_method` varchar(2048) DEFAULT NULL,
  `call_class_method` varchar(2048) DEFAULT NULL,
  `project_name` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;