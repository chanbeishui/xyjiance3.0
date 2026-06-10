package com.xyjiance.system.service;

import java.util.List;
import com.xyjiance.common.core.domain.TreeSelect;
import com.xyjiance.common.core.domain.entity.SysCompany;

/**
 * 公司管理 Service 接口
 *
 * @author xyjiance
 */
public interface ISysCompanyService
{
    /**
     * 查询公司列表
     */
    public List<SysCompany> selectCompanyList(SysCompany company);

    /**
     * 根据ID查询公司
     */
    public SysCompany selectCompanyById(Long companyId);

    /**
     * 构建前端下拉树结构
     */
    public List<TreeSelect> buildCompanyTreeSelect(List<SysCompany> companies);

    /**
     * 构建前端树形结构
     */
    public List<SysCompany> buildCompanyTree(List<SysCompany> companies);

    /**
     * 新增公司
     */
    public int insertCompany(SysCompany company);

    /**
     * 修改公司
     */
    public int updateCompany(SysCompany company);

    /**
     * 删除公司
     */
    public int deleteCompanyById(Long companyId);

    /**
     * 校验公司名称唯一
     */
    public boolean checkCompanyNameUnique(SysCompany company);
}
