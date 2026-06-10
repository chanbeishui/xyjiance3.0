package com.xyjiance.web.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.xyjiance.common.constant.Constants;
import com.xyjiance.common.core.domain.AjaxResult;
import com.xyjiance.common.core.domain.model.WxLoginBody;
import com.xyjiance.framework.web.service.WxLoginService;
import com.xyjiance.framework.web.service.WxUserNotBoundException;

/**
 * 微信小程序登录接口
 *
 * @author xyjiance
 */
@RestController
public class SysWxLoginController
{
    @Autowired
    private WxLoginService wxLoginService;

    /**
     * 微信小程序登录
     *
     * @param wxLoginBody 微信登录请求体
     * @return token
     */
    @PostMapping("/wx/login")
    public AjaxResult wxLogin(@RequestBody WxLoginBody wxLoginBody)
    {
        AjaxResult ajax = AjaxResult.success();
        try
        {
            String token = wxLoginService.wxLogin(wxLoginBody.getCode());
            ajax.put(Constants.TOKEN, token);
        }
        catch (WxUserNotBoundException e)
        {
            // 用户未绑定：返回特殊状态码，前端引导用户绑定已有账号
            return AjaxResult.error(601, "微信未绑定系统账号，请先登录后绑定");
        }
        return ajax;
    }
}
