package com.easysoftwareinput.infrastructure.rpmpkg;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.easysoftwareinput.domain.rpmpackage.model.RPMPackageDO;
import com.easysoftwareinput.infrastructure.mapper.RPMPackageDOMapper;

@Component
public class RpmGatewayImpl {
    /**
     * mapper.
     */
    @Autowired
    private RPMPackageDOMapper mapper;

    /**
     * get distinct os from table.
     * @return list of os.
     */
    public List<String> getOs() {
        QueryWrapper<RPMPackageDO> wrapper = new QueryWrapper<>();
        wrapper.select("distinct os");
        wrapper.eq("sub_path", "everythingx86_64");
        List<RPMPackageDO> doList = mapper.selectList(wrapper);
        List<String> osList = new ArrayList<>();
        for (RPMPackageDO pkg : doList) {
            osList.add(pkg.getOs());
        }
        return osList;
    }

    /**
     * get pkg by os.
     * @param os os.
     * @return lsit of pkgs.
     */
    public List<RPMPackageDO> getPkg(String os) {
        QueryWrapper<RPMPackageDO> wrapper = new QueryWrapper<>();
        wrapper.select("os, arch, name, version, category, pkg_id, description");
        wrapper.eq("sub_path", "everythingx86_64");
        wrapper.eq("os", os);
        return mapper.selectList(wrapper);
    }

    /**
     * get pkgs which will be converted to domain pkg.
     * @return list of pkg.
     */
    public List<RPMPackageDO> getDomain() {
        QueryWrapper<RPMPackageDO> wrapper = new QueryWrapper<>();
        wrapper.in("category", List.of("AI", "大数据", "分布式存储", "数据库", "云服务", "HPC"));
        wrapper.select("os, arch, name, version, category, pkg_id, description");
        wrapper.groupBy("name");
        return mapper.selectList(wrapper);
    }

    /**
     * get one pkg by name.
     * @param name name.
     * @return one pkg.
     */
    public RPMPackageDO queryPkgIdByName(String name) {
        QueryWrapper<RPMPackageDO> wrapper = new QueryWrapper<>();
        wrapper.select("name, pkg_id");
        wrapper.eq("name", name);
        wrapper.last("limit 1");
        List<RPMPackageDO> list = mapper.selectList(wrapper);
        if (list.size() >= 1) {
            return list.get(0);
        }
        return new RPMPackageDO();
    }
}
