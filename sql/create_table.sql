

-- 创建库
create database if not exists wang_ai_code_mother;
use wang_ai_code_mother;
-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
    ) comment '用户' collate = utf8mb4_unicode_ci;
-- 应用表
create table app
(
    id           bigint auto_increment comment 'id' primary key,
    appName      varchar(256)                       null comment '应用名称',
    cover        varchar(512)                       null comment '应用封面',
    initPrompt   text                               null comment '应用初始化的 prompt',
    codeGenType  varchar(64)                        null comment '代码生成类型（枚举）',
    deployKey    varchar(64)                        null comment '部署标识',
    deployedTime datetime                           null comment '部署时间',
    priority     int      default 0                 not null comment '优先级',
    userId       bigint                             not null comment '创建用户id',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    UNIQUE KEY uk_deployKey (deployKey), -- 确保部署标识唯一
    INDEX idx_appName (appName),         -- 提升基于应用名称的查询性能
    INDEX idx_userId (userId)            -- 提升基于用户 ID 的查询性能
) comment '应用' collate = utf8mb4_unicode_ci;

-- 对话历史表
create table chat_history
(
    id          bigint auto_increment comment 'id' primary key,
    message     text                               not null comment '消息',
    messageType varchar(32)                        not null comment 'user/ai',
    appId       bigint                             not null comment '应用id',
    userId      bigint                             not null comment '创建用户id',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    INDEX idx_appId (appId),                       -- 提升基于应用的查询性能
    INDEX idx_createTime (createTime),             -- 提升基于时间的查询性能
    INDEX idx_appId_createTime (appId, createTime) -- 游标查询核心索引
) comment '对话历史' collate = utf8mb4_unicode_ci;

-- AI文章核心表
create table if not exists ai_articles
(
    id              bigint auto_increment comment '文章唯一标识' primary key,
    title           varchar(255)                        not null comment '文章标题',
    source_url      varchar(500)                        null comment '原文链接',
    source_platform varchar(100)                        null comment '爬取来源平台',
    author          varchar(100)                        null comment '文章作者',
    publish_time    datetime                            not null comment '文章发布时间',
    content         longtext                            null comment '正文内容',
    category        varchar(50)                         null comment '文章分类',
    is_featured     tinyint  default 0                  not null comment '是否精选 0=否 1=是',
    crawl_time      datetime default CURRENT_TIMESTAMP  not null comment '爬取时间',
    status          varchar(20) default 'normal'        not null comment '状态 normal=正常 down=下架 pending=待审核',
    unique_key      varchar(64)                         null comment '去重MD5值',
    views           int      default 0                  not null comment '阅读量',
    UNIQUE KEY uk_source_url (source_url),
    UNIQUE KEY uk_unique_key (unique_key),
    INDEX idx_title (title),
    INDEX idx_publish_time (publish_time),
    INDEX idx_category (category),
    INDEX idx_status (status)
) comment 'AI文章核心表' collate = utf8mb4_unicode_ci;

-- 爬虫任务日志表
create table if not exists crawl_task_logs
(
    id            bigint auto_increment comment '日志唯一标识' primary key,
    task_date     date                               not null comment '爬取任务日期',
    start_time    datetime                           not null comment '任务开始时间',
    end_time      datetime                           null comment '任务结束时间',
    total_crawl   int      default 0                 not null comment '当日爬取总数量',
    success_count int      default 0                 not null comment '成功入库数量',
    fail_count    int      default 0                 not null comment '失败数量',
    fail_reason   text                               null comment '失败原因',
    task_status   varchar(20) default 'running'      not null comment '任务状态 running/finished/failed',
    operator      varchar(50)                        null comment '执行人 auto=自动',
    INDEX idx_task_date (task_date),
    INDEX idx_task_status (task_status)
) comment '爬虫任务日志表' collate = utf8mb4_unicode_ci;

