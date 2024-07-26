/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80033 (8.0.33)
 Source Host           : localhost:3306
 Source Schema         : x_pan

 Target Server Type    : MySQL
 Target Server Version : 80033 (8.0.33)
 File Encoding         : 65001

 Date: 26/07/2024 12:23:51
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for x_pan_error_log
-- ----------------------------
DROP TABLE IF EXISTS `x_pan_error_log`;
CREATE TABLE `x_pan_error_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `log_content` varchar(900) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '日志内容',
  `log_status` tinyint NULL DEFAULT 0 COMMENT '日志状态：0 未处理 1 已处理',
  `create_user` bigint NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user` bigint NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '错误日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of x_pan_error_log
-- ----------------------------

-- ----------------------------
-- Table structure for x_pan_file
-- ----------------------------
DROP TABLE IF EXISTS `x_pan_file`;
CREATE TABLE `x_pan_file`  (
  `file_id` bigint NOT NULL COMMENT '文件id',
  `filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '文件名称',
  `real_path` varchar(700) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '文件物理路径',
  `file_size` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '文件实际大小',
  `file_size_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '文件大小展示字符',
  `file_suffix` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '文件后缀',
  `file_preview_content_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '文件预览的响应头Content-Type的值',
  `identifier` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '文件唯一标识',
  `create_user` bigint NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`file_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '物理文件信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of x_pan_file
-- ----------------------------

-- ----------------------------
-- Table structure for x_pan_file_chunk
-- ----------------------------
DROP TABLE IF EXISTS `x_pan_file_chunk`;
CREATE TABLE `x_pan_file_chunk`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `identifier` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '文件唯一标识',
  `real_path` varchar(700) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '分片真实的存储路径',
  `chunk_number` int NOT NULL DEFAULT 0 COMMENT '分片编号',
  `expiration_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '过期时间',
  `create_user` bigint NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_identifier_chunk_number_create_user`(`identifier` ASC, `chunk_number` ASC, `create_user` ASC) USING BTREE COMMENT '文件唯一标识、分片编号和用户ID的唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 101 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '文件分片信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of x_pan_file_chunk
-- ----------------------------

-- ----------------------------
-- Table structure for x_pan_share
-- ----------------------------
DROP TABLE IF EXISTS `x_pan_share`;
CREATE TABLE `x_pan_share`  (
  `share_id` bigint NOT NULL COMMENT '分享id',
  `share_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '分享名称',
  `share_type` tinyint(1) NOT NULL DEFAULT 0 COMMENT '分享类型（0 有提取码）',
  `share_day_type` tinyint(1) NOT NULL DEFAULT 0 COMMENT '分享类型（0 永久有效；1 7天有效；2 30天有效）',
  `share_day` tinyint(1) NOT NULL DEFAULT 0 COMMENT '分享有效天数（永久有效为0）',
  `share_end_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分享结束时间',
  `share_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '分享链接地址',
  `share_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '分享提取码',
  `share_status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '分享状态（0 正常；1 有文件被删除）',
  `create_user` bigint NOT NULL COMMENT '分享创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`share_id`) USING BTREE,
  UNIQUE INDEX `uk_create_user_time`(`create_user` ASC, `create_time` ASC) USING BTREE COMMENT '创建人、创建时间唯一索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '用户分享表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of x_pan_share
-- ----------------------------

-- ----------------------------
-- Table structure for x_pan_share_file
-- ----------------------------
DROP TABLE IF EXISTS `x_pan_share_file`;
CREATE TABLE `x_pan_share_file`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `share_id` bigint NOT NULL COMMENT '分享id',
  `file_id` bigint NOT NULL COMMENT '文件记录ID',
  `create_user` bigint NOT NULL COMMENT '分享创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_share_id_file_id`(`share_id` ASC, `file_id` ASC) USING BTREE COMMENT '分享ID、文件ID联合唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '用户分享文件表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of x_pan_share_file
-- ----------------------------

-- ----------------------------
-- Table structure for x_pan_user
-- ----------------------------
DROP TABLE IF EXISTS `x_pan_user`;
CREATE TABLE `x_pan_user`  (
  `user_id` bigint NOT NULL COMMENT '用户id',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '密码',
  `salt` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '随机盐值',
  `question` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '密保问题',
  `answer` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '密保答案',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE COMMENT '用户名唯一索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '用户信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of x_pan_user
-- ----------------------------

-- ----------------------------
-- Table structure for x_pan_user_file
-- ----------------------------
DROP TABLE IF EXISTS `x_pan_user_file`;
CREATE TABLE `x_pan_user_file`  (
  `file_id` bigint NOT NULL COMMENT '文件记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `parent_id` bigint NOT NULL COMMENT '上级文件夹ID,顶级文件夹为0',
  `real_file_id` bigint NOT NULL DEFAULT 0 COMMENT '真实文件id',
  `filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '文件名',
  `folder_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否是文件夹 （0 否 1 是）',
  `file_size_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '--' COMMENT '文件大小展示字符',
  `file_type` tinyint(1) NOT NULL DEFAULT 0 COMMENT '文件类型（1 普通文件 2 压缩文件 3 excel 4 word 5 pdf 6 txt 7 图片 8 音频 9 视频 10 ppt 11 源码文件 12 csv）',
  `del_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标识（0 否 1 是）',
  `create_user` bigint NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user` bigint NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`file_id`) USING BTREE,
  INDEX `index_file_list`(`user_id` ASC, `del_flag` ASC, `parent_id` ASC, `file_type` ASC, `file_id` ASC, `filename` ASC, `folder_flag` ASC, `file_size_desc` ASC, `create_time` ASC, `update_time` ASC) USING BTREE COMMENT '查询文件列表索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '用户文件信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of x_pan_user_file
-- ----------------------------

-- ----------------------------
-- Table structure for x_pan_user_search_history
-- ----------------------------
DROP TABLE IF EXISTS `x_pan_user_search_history`;
CREATE TABLE `x_pan_user_search_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `search_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '搜索文案',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id_search_content_update_time`(`user_id` ASC, `search_content` ASC, `update_time` ASC) USING BTREE COMMENT '用户id、搜索内容和更新时间唯一索引',
  UNIQUE INDEX `uk_user_id_search_content`(`user_id` ASC, `search_content` ASC) USING BTREE COMMENT '用户id和搜索内容唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '用户搜索历史表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of x_pan_user_search_history
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
