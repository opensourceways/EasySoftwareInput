package com.easysoftwareinput.infrastructure.archnum.converter;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.easysoftwareinput.infrastructure.archnum.OsArchNumDO;
import com.easysoftwareinput.infrastructure.rpmpkg.IDataObject;

@Component
public class ArchNumConverter {
    /**
     * convert pkg to OsArchNumDO.
     * @param <T> generic type.
     * @param list list of pkgs.
     * @param type type of table.
     * @return list of OsArchNumDO.
     */
    public <T extends IDataObject> List<OsArchNumDO> ofList(List<T> list, String type) {
        return list.stream().map(e -> this.ofPkg(e, type)).collect(Collectors.toList());
    }

    /**
     * convert pkg to OsArchNumDO.
     * @param <T> generic type.
     * @param e pkg.
     * @param type type of table.
     * @return OsArchNumDO.
     */
    public <T extends IDataObject> OsArchNumDO ofPkg(T e, String type) {
        OsArchNumDO res = new OsArchNumDO();
        res.setArchName(e.getArch());
        res.setOs(e.getOs());
        res.setCount(e.getCount());

        res.setType(type);
        res.setUpdateAt(new Timestamp(System.currentTimeMillis()));
        res.setPkgId(res.getOs() + res.getType() + res.getArchName());
        return res;
    }
}
