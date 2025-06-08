-- Create conversation table
CREATE TABLE `conversation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(255) NOT NULL COMMENT '对话标题',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `model_id` bigint(20) DEFAULT NULL COMMENT '模型ID',
  `model_config` json DEFAULT NULL COMMENT '模型配置',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态 0:已归档 1:活跃',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标记 0:未删除 1:已删除',
  `version` int(11) NOT NULL DEFAULT '1' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话表';

-- Create chat_message table
CREATE TABLE `chat_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `conversation_id` bigint(20) NOT NULL COMMENT '对话ID',
  `role` varchar(50) NOT NULL COMMENT '消息角色 user/assistant/system',
  `content` text NOT NULL COMMENT '消息内容',
  `sequence` int(11) NOT NULL COMMENT '消息顺序',
  `token_count` int(11) DEFAULT NULL COMMENT 'Token数量',
  `model_id` varchar(50) DEFAULT NULL COMMENT '模型ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标记 0:未删除 1:已删除',
  `version` int(11) NOT NULL DEFAULT '1' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  KEY `idx_conversation_id` (`conversation_id`),
  KEY `idx_sequence` (`conversation_id`, `sequence`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表'; 