package com.easysoftwareinput.infrastructure.epkgpkg;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.easysoftwareinput.infrastructure.epkgpkg.dataobject.EpkgDo;
import com.easysoftwareinput.infrastructure.mapper.EpkgDoMapper;

@Component
public class EpkgGatewayImpl {
    /**
     * mapper.
     */
    @Autowired
    private EpkgDoMapper mapper;

    /**
     * get distinct os from table.
     * @return list of os.
     */
    public List<String> getOs() {
        QueryWrapper<EpkgDo> wrapper = new QueryWrapper<>();
        wrapper.select("distinct os");
        List<EpkgDo> doList = mapper.selectList(wrapper);
        List<String> osList = new ArrayList<>();
        for (EpkgDo pkg : doList) {
            osList.add(pkg.getOs());
        }
        return osList;
    }

    /**
     * get list of pkgs by os.
     * @param os os.
     * @return list of pkgs.
     */
    public List<EpkgDo> getPkg(String os) {
        QueryWrapper<EpkgDo> wrapper = new QueryWrapper<>();
        wrapper.select("os, arch, name, version, category, pkg_id, description");
        wrapper.eq("os", os);
        return mapper.selectList(wrapper);
    }

    /**
     * get one pkgid by name.
     * @param name name.
     * @return one pkgid.
     */
    public EpkgDo queryPkgIdByName(String name) {
        QueryWrapper<EpkgDo> wrapper = new QueryWrapper<>();
        wrapper.select("name, pkg_id");
        wrapper.eq("name", name);
        wrapper.last("limit 1");
        List<EpkgDo> list = mapper.selectList(wrapper);
        if (list.size() >= 1) {
            return list.get(0);
        }
        return new EpkgDo();
    }
}
