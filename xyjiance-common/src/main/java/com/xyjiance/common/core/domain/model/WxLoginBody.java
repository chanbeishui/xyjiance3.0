package com.xyjiance.common.core.domain.model;

/**
 * 微信小程序登录请求体
 *
 * @author xyjiance
 */
public class WxLoginBody
{
    /** wx.login() 返回的临时 code */
    private String code;

    /** 微信加密用户数据（可选，用于获取详细信息） */
    private String encryptedData;

    /** 加密算法初始向量（可选） */
    private String iv;

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getEncryptedData()
    {
        return encryptedData;
    }

    public void setEncryptedData(String encryptedData)
    {
        this.encryptedData = encryptedData;
    }

    public String getIv()
    {
        return iv;
    }

    public void setIv(String iv)
    {
        this.iv = iv;
    }
}
