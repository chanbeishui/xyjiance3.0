-- ----------------------------
-- 微信小程序支持 - 数据库变更
-- ----------------------------

-- sys_user 表添加微信字段
ALTER TABLE sys_user ADD COLUMN wx_open_id varchar(128) DEFAULT '' COMMENT '微信小程序 openId';
ALTER TABLE sys_user ADD COLUMN wx_union_id varchar(128) DEFAULT '' COMMENT '微信开放平台 unionId';

-- 添加索引
CREATE INDEX idx_sys_user_wx_open_id ON sys_user(wx_open_id);
