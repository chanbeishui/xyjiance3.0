package com.xyjiance.framework.web.service;

/**
 * 微信用户未绑定异常
 *
 * @author xyjiance
 */
public class WxUserNotBoundException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    private String openId;

    public WxUserNotBoundException(String openId)
    {
        super("微信用户未绑定系统账号，openId: " + openId);
        this.openId = openId;
    }

    public String getOpenId()
    {
        return openId;
    }
}
