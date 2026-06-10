package com.xyjiance.system.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xyjiance.common.constant.UserConstants;
import com.xyjiance.common.core.domain.TreeSelect;
import com.xyjiance.common.core.domain.entity.SysCompany;
import com.xyjiance.common.utils.SecurityUtils;
import com.xyjiance.common.utils.StringUtils;
import com.xyjiance.system.mapper.SysCompanyMapper;
import com.xyjiance.system.service.ISysCompanyService;

/**
 * 公司管理 Service 实现
 *
 * @author xyjiance
 */
@Service
public class SysCompanyServiceImpl implements ISysCompanyService
{
    @Autowired
    private SysCompanyMapper companyMapper;

    @Override
    public List<SysCompany> selectCompanyList(SysCompany company)
    {
        return companyMapper.selectCompanyList(company);
    }

    @Override
    public SysCompany selectCompanyById(Long companyId)
    {
        return companyMapper.selectCompanyById(companyId);
    }

    @Override
    public List<TreeSelect> buildCompanyTreeSelect(List<SysCompany> companies)
    {
        List<SysCompany> companyTrees = buildCompanyTree(companies);
        return companyTrees.stream().map(TreeSelect::new).collect(Collectors.toList());
    }

    @Override
    public List<SysCompany> buildCompanyTree(List<SysCompany> companies)
    {
        List<SysCompany> returnList = new ArrayList<>();
        List<Long> tempList = companies.stream().map(SysCompany::getCompanyId).collect(Collectors.toList());
        for (SysCompany company : companies)
        {
            if (!tempList.contains(company.getParentId()))
            {
                recursionFn(companies, company);
                returnList.add(company);
            }
        }
        if (returnList.isEmpty())
        {
            returnList = companies;
        }
        return returnList;
    }

    private void recursionFn(List<SysCompany> list, SysCompany t)
    {
        List<SysCompany> children = getChildList(list, t);
        t.setChildren(children);
        for (SysCompany tChild : children)
        {
            if (hasChild(list, tChild))
            {
                recursionFn(list, tChild);
            }
        }
    }

    private List<SysCompany> getChildList(List<SysCompany> list, SysCompany t)
    {
        List<SysCompany> tlist = new ArrayList<>();
        for (SysCompany n : list)
        {
            if (StringUtils.isNotNull(n.getParentId()) && n.getParentId().longValue() == t.getCompanyId().longValue())
            {
                tlist.add(n);
            }
        }
        return tlist;
    }

    private boolean hasChild(List<SysCompany> list, SysCompany t)
    {
        return getChildList(list, t).size() > 0;
    }

    @Override
    public int insertCompany(SysCompany company)
    {
        SysCompany info = companyMapper.selectCompanyById(company.getParentId());
        if (info != null)
        {
            company.setAncestors(info.getAncestors() + "," + company.getParentId());
        }
        else
        {
            company.setAncestors("0");
        }
        company.setCreateBy(SecurityUtils.getUsername());
        return companyMapper.insertCompany(company);
    }

    @Override
    public int updateCompany(SysCompany company)
    {
        SysCompany newParent = companyMapper.selectCompanyById(company.getParentId());
        SysCompany old = companyMapper.selectCompanyById(company.getCompanyId());
        if (newParent != null && old != null)
        {
            String newAncestors = newParent.getAncestors() + "," + newParent.getCompanyId();
            String oldAncestors = old.getAncestors();
            company.setAncestors(newAncestors);
            // 更新所有子公司的祖级路径
            updateCompanyChildren(company.getCompanyId(), newAncestors, oldAncestors);
        }
        company.setUpdateBy(SecurityUtils.getUsername());
        return companyMapper.updateCompany(company);
    }

    private void updateCompanyChildren(Long companyId, String newAncestors, String oldAncestors)
    {
        List<SysCompany> children = companyMapper.selectChildrenCompanyById(companyId);
        for (SysCompany child : children)
        {
            child.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
            companyMapper.updateCompany(child);
        }
    }

    @Override
    public int deleteCompanyById(Long companyId)
    {
        return companyMapper.deleteCompanyById(companyId);
    }

    @Override
    public boolean checkCompanyNameUnique(SysCompany company)
    {
        Long companyId = StringUtils.isNull(company.getCompanyId()) ? -1L : company.getCompanyId();
        SysCompany info = companyMapper.checkCompanyNameUnique(company.getCompanyName(), company.getParentId());
        if (StringUtils.isNotNull(info) && info.getCompanyId().longValue() != companyId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }
}
