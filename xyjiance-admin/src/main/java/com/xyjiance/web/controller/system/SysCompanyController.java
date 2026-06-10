package com.xyjiance.web.controller.system;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.xyjiance.common.annotation.Log;
import com.xyjiance.common.constant.UserConstants;
import com.xyjiance.common.core.controller.BaseController;
import com.xyjiance.common.core.domain.AjaxResult;
import com.xyjiance.common.core.domain.entity.SysCompany;
import com.xyjiance.common.enums.BusinessType;
import com.xyjiance.common.utils.StringUtils;
import com.xyjiance.system.service.ISysCompanyService;

/**
 * 公司管理 Controller
 *
 * @author xyjiance
 */
@RestController
@RequestMapping("/system/company")
public class SysCompanyController extends BaseController
{
    @Autowired
    private ISysCompanyService companyService;

    /**
     * 获取公司列表
     */
    @PreAuthorize("@ss.hasPermi('system:company:list')")
    @GetMapping("/list")
    public AjaxResult list(SysCompany company)
    {
        List<SysCompany> companies = companyService.selectCompanyList(company);
        return success(companies);
    }

    /**
     * 查询公司列表（排除节点）
     */
    @PreAuthorize("@ss.hasPermi('system:company:list')")
    @GetMapping("/list/exclude/{companyId}")
    public AjaxResult excludeChild(@PathVariable Long companyId)
    {
        List<SysCompany> companies = companyService.selectCompanyList(new SysCompany());
        companies.removeIf(c -> c.getCompanyId().equals(companyId)
                || StringUtils.splitList(c.getAncestors()).contains(String.valueOf(companyId)));
        return success(companies);
    }

    /**
     * 根据公司ID获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:company:query')")
    @GetMapping("/{companyId}")
    public AjaxResult getInfo(@PathVariable Long companyId)
    {
        return success(companyService.selectCompanyById(companyId));
    }

    /**
     * 获取公司下拉树列表
     */
    @GetMapping("/treeselect")
    public AjaxResult treeselect(SysCompany company)
    {
        List<SysCompany> companies = companyService.selectCompanyList(company);
        return success(companyService.buildCompanyTreeSelect(companies));
    }

    /**
     * 新增公司
     */
    @PreAuthorize("@ss.hasPermi('system:company:add')")
    @Log(title = "公司管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysCompany company)
    {
        if (!companyService.checkCompanyNameUnique(company))
        {
            return error("新增公司'" + company.getCompanyName() + "'失败，公司名称已存在");
        }
        company.setCreateBy(getUsername());
        return toAjax(companyService.insertCompany(company));
    }

    /**
     * 修改公司
     */
    @PreAuthorize("@ss.hasPermi('system:company:edit')")
    @Log(title = "公司管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysCompany company)
    {
        Long companyId = company.getCompanyId();
        if (company.getParentId().equals(companyId))
        {
            return error("修改公司'" + company.getCompanyName() + "'失败，上级公司不能是自己");
        }
        if (!companyService.checkCompanyNameUnique(company))
        {
            return error("修改公司'" + company.getCompanyName() + "'失败，公司名称已存在");
        }
        company.setUpdateBy(getUsername());
        return toAjax(companyService.updateCompany(company));
    }

    /**
     * 删除公司
     */
    @PreAuthorize("@ss.hasPermi('system:company:remove')")
    @Log(title = "公司管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{companyId}")
    public AjaxResult remove(@PathVariable Long companyId)
    {
        // TODO: 检查是否有部门和用户关联该公司
        return toAjax(companyService.deleteCompanyById(companyId));
    }
}
