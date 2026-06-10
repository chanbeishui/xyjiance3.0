-- ----------------------------
-- 集团架构支持 - 数据库变更
-- ----------------------------

-- 1、公司表
DROP TABLE IF EXISTS sys_company;
CREATE TABLE sys_company (
  company_id        bigint(20)      NOT NULL AUTO_INCREMENT    COMMENT '公司ID',
  parent_id         bigint(20)      DEFAULT 0                  COMMENT '父公司ID',
  ancestors         varchar(500)    DEFAULT ''                 COMMENT '祖级列表',
  company_name      varchar(60)     NOT NULL                   COMMENT '公司名称',
  order_num         int(4)          DEFAULT 0                  COMMENT '显示顺序',
  leader            varchar(20)     DEFAULT NULL               COMMENT '负责人',
  phone             varchar(11)     DEFAULT NULL               COMMENT '联系电话',
  email             varchar(50)     DEFAULT NULL               COMMENT '邮箱',
  status            char(1)         DEFAULT '0'                COMMENT '公司状态（0正常 1停用）',
  del_flag          char(1)         DEFAULT '0'                COMMENT '删除标志（0代表存在 2代表删除）',
  create_by         varchar(64)     DEFAULT ''                 COMMENT '创建者',
  create_time       datetime                                   COMMENT '创建时间',
  update_by         varchar(64)     DEFAULT ''                 COMMENT '更新者',
  update_time       datetime                                   COMMENT '更新时间',
  PRIMARY KEY (company_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT = '公司表';

-- 初始化集团总公司
INSERT INTO sys_company VALUES (1, 0, '0', '集信集团', 1, '管理员', '13800000000', 'group@xyjiance.com', '0', '0', 'admin', sysdate(), '', NULL);

-- 2、部门表增加公司关联
ALTER TABLE sys_dept ADD COLUMN company_id bigint(20) DEFAULT 1 COMMENT '所属公司ID';
-- 将现有部门关联到总公司
UPDATE sys_dept SET company_id = 1 WHERE company_id IS NULL;

-- 3、用户表增加公司关联
ALTER TABLE sys_user ADD COLUMN company_id bigint(20) DEFAULT 1 COMMENT '所属公司ID';
-- 将现有用户关联到总公司
UPDATE sys_user SET company_id = 1 WHERE company_id IS NULL;

-- 4、微信小程序支持（前期有过独立迁移脚本则跳过）
-- 检查列是否存在，不存在则添加
-- ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS wx_open_id varchar(128) DEFAULT '' COMMENT '微信小程序 openId';
-- ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS wx_union_id varchar(128) DEFAULT '' COMMENT '微信开放平台 unionId';
