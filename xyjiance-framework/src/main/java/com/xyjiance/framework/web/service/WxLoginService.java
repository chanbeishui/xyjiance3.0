package com.xyjiance.framework.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.xyjiance.common.core.domain.entity.SysUser;
import com.xyjiance.common.core.domain.model.LoginUser;
import com.xyjiance.system.mapper.SysUserMapper;
import com.xyjiance.system.service.ISysUserService;

/**
 * 微信小程序登录服务
 *
 * @author xyjiance
 */
@Component
public class WxLoginService
{
    @Autowired
    private TokenService tokenService;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private SysPermissionService permissionService;

    /**
     * 微信小程序登录
     * 流程：code → openId → 查找已绑定用户 → 生成 token
     *
     * @param code wx.login() 返回的临时 code
     * @return token
     */
    public String wxLogin(String code)
    {
        // TODO: 调用微信 code2session 接口获取 openId 和 unionId
        // 示例代码（需配置小程序的 appId 和 appSecret）：
        // String url = "https://api.weixin.qq.com/sns/jscode2session" +
        //     "?appid=" + wxAppId + "&secret=" + wxSecret +
        //     "&js_code=" + code + "&grant_type=authorization_code";
        // String result = HttpUtils.sendGet(url);
        // JSONObject json = JSON.parseObject(result);
        // String openId = json.getString("openid");
        // String unionId = json.getString("unionid");

        // 临时方案：使用 code 作为 openId（生产环境需替换为真实微信接口调用）
        String openId = code; // TODO: 替换为真实 openId

        // 根据 openId 查找已绑定的用户
        SysUser user = userMapper.selectUserByOpenId(openId);

        if (user == null)
        {
            // 用户未绑定微信，返回特殊状态让前端引导绑定已有账号
            throw new WxUserNotBoundException(openId);
        }

        // 生成登录 token
        LoginUser loginUser = new LoginUser(user, permissionService.getMenuPermission(user));
        return tokenService.createToken(loginUser);
    }
}
