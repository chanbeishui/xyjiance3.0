package com.xyjiance.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.xyjiance.common.core.domain.entity.SysCompany;

/**
 * 公司管理 数据层
 *
 * @author xyjiance
 */
public interface SysCompanyMapper
{
    /**
     * 查询公司列表
     *
     * @param company 公司信息
     * @return 公司集合
     */
    public List<SysCompany> selectCompanyList(SysCompany company);

    /**
     * 根据公司ID查询
     */
    public SysCompany selectCompanyById(Long companyId);

    /**
     * 新增公司
     */
    public int insertCompany(SysCompany company);

    /**
     * 修改公司
     */
    public int updateCompany(SysCompany company);

    /**
     * 删除公司（逻辑删除）
     */
    public int deleteCompanyById(Long companyId);

    /**
     * 根据ID查询所有子节点
     */
    public List<SysCompany> selectChildrenCompanyById(Long companyId);

    /**
     * 是否存在子节点
     */
    public int hasChildByCompanyId(Long companyId);

    /**
     * 校验公司名称是否唯一
     */
    public SysCompany checkCompanyNameUnique(@Param("companyName") String companyName, @Param("parentId") Long parentId);
}
