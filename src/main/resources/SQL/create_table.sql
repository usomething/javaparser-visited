drop table autopart_method_call;

CREATE TABLE `autopart_method_call` (
  `id` int NOT NULL AUTO_INCREMENT,
  `class_name` varchar(255) DEFAULT NULL,
  `method_name` varchar(255) DEFAULT NULL,
  `method_param_count` int DEFAULT NULL,
  `method_param_type` varchar(2048) DEFAULT NULL,
  `call_method` varchar(2048) DEFAULT NULL,
  `call_class_method` varchar(2048) DEFAULT NULL,
  `call_method_param_count` int DEFAULT NULL,
  `call_method_param_type` varchar(2048) DEFAULT NULL,
  `project_name` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
);