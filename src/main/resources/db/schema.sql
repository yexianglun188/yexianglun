-- ============================================================
-- 校园二手交易平台 - 数据库结构
-- 数据库：MySQL 8.0
-- ============================================================

CREATE DATABASE IF NOT EXISTS campus_market
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE campus_market;

-- ------------------------------------------------------------
-- 用户表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(50)  NOT NULL COMMENT '登录账号',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `student_no`  VARCHAR(30)  DEFAULT NULL COMMENT '学号',
    `campus`      VARCHAR(100) DEFAULT NULL COMMENT '所在校区',
    `role`        TINYINT      NOT NULL DEFAULT 0 COMMENT '角色 0普通用户 1管理员',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 1正常 0禁用',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_role` (`role`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- ------------------------------------------------------------
-- 商品分类表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`        VARCHAR(50) NOT NULL COMMENT '分类名称',
    `icon`        VARCHAR(50) DEFAULT NULL COMMENT '图标标识',
    `sort`        INT         NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    `deleted`     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品分类表';

-- ------------------------------------------------------------
-- 商品表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`        BIGINT        NOT NULL COMMENT '卖家用户id',
    `category_id`    BIGINT        NOT NULL COMMENT '分类id',
    `title`          VARCHAR(100)  NOT NULL COMMENT '商品标题',
    `description`    TEXT          COMMENT '商品描述',
    `price`          DECIMAL(10, 2) NOT NULL COMMENT '售价',
    `original_price` DECIMAL(10, 2) DEFAULT NULL COMMENT '原价',
    `cover_image`    VARCHAR(255)  DEFAULT NULL COMMENT '封面图',
    `images`         VARCHAR(2000) DEFAULT NULL COMMENT '图片列表，逗号分隔',
    `item_condition` VARCHAR(20)   DEFAULT '几乎全新' COMMENT '成色',
    `trade_place`    VARCHAR(100)  DEFAULT NULL COMMENT '交易地点',
    `status`         TINYINT       NOT NULL DEFAULT 0 COMMENT '状态 0待审核 1在售 2已售 3已下架',
    `view_count`     INT           NOT NULL DEFAULT 0 COMMENT '浏览量',
    `favorite_count` INT           NOT NULL DEFAULT 0 COMMENT '收藏数',
    `deleted`        TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_category` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品表';

-- ------------------------------------------------------------
-- 订单表
-- 状态：0待支付 1待卖家确认 2交易中 3已完成 4已取消
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_no`    VARCHAR(32)    NOT NULL COMMENT '订单编号',
    `product_id`  BIGINT         NOT NULL COMMENT '商品id',
    `buyer_id`    BIGINT         NOT NULL COMMENT '买家id',
    `seller_id`   BIGINT         NOT NULL COMMENT '卖家id',
    `amount`      DECIMAL(10, 2) NOT NULL COMMENT '成交金额',
    `status`      TINYINT        NOT NULL DEFAULT 0 COMMENT '状态 0待支付 1待卖家确认 2交易中 3已完成 4已取消',
    `remark`      VARCHAR(255)   DEFAULT NULL COMMENT '买家留言',
    `contact`     VARCHAR(50)    DEFAULT NULL COMMENT '联系方式',
    `paid_time`   DATETIME       DEFAULT NULL COMMENT '支付时间',
    `deleted`     TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `finish_time` DATETIME       DEFAULT NULL COMMENT '完成时间',
    `create_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    `update_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_buyer` (`buyer_id`),
    KEY `idx_seller` (`seller_id`),
    KEY `idx_product` (`product_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单表';

-- ------------------------------------------------------------
-- 收藏表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT   NOT NULL COMMENT '用户id',
    `product_id`  BIGINT   NOT NULL COMMENT '商品id',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '收藏表';

-- ------------------------------------------------------------
-- 留言表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `product_id`  BIGINT       NOT NULL COMMENT '商品id',
    `user_id`     BIGINT       NOT NULL COMMENT '留言用户id',
    `content`     VARCHAR(500) NOT NULL COMMENT '留言内容',
    `parent_id`   BIGINT       NOT NULL DEFAULT 0 COMMENT '父留言id，0为顶级',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '留言时间',
    PRIMARY KEY (`id`),
    KEY `idx_product` (`product_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '留言表';

-- ------------------------------------------------------------
-- 私信消息表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `from_id`     BIGINT       NOT NULL COMMENT '发送者id',
    `to_id`       BIGINT       NOT NULL COMMENT '接收者id',
    `product_id`  BIGINT       DEFAULT NULL COMMENT '关联商品id(可选)',
    `content`     VARCHAR(1000) NOT NULL COMMENT '消息内容',
    `is_read`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读 0未读 1已读',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (`id`),
    KEY `idx_to` (`to_id`),
    KEY `idx_from_to` (`from_id`, `to_id`),
    KEY `idx_product` (`product_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '私信消息表';
