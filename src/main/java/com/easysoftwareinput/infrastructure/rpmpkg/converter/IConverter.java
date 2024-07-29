package com.easysoftwareinput.infrastructure.rpmpkg.converter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.easysoftwareinput.infrastructure.rpmpkg.IDataObject;

public interface IConverter {
    /**
     * pick one from list.
     * @param list list.
     * @return one.
     */
    IDataObject pickOneFromList(List<IDataObject> list);

    /**
     * group list by verion.
     * @param list list.
     * @return map.
     */
    Map<String, List<IDataObject>> getVersionMap(List<IDataObject> list);

    /**
     * get latest version of pkgs.
     * @param list list.
     * @return latest version of pkgs.
     */
    default List<IDataObject> getLatestVersion(List<IDataObject> list) {
        Map<String, List<IDataObject>> versionMap = getVersionMap(list);
        List<String> sortedVersions = versionMap.keySet().stream().sorted().collect(Collectors.toList());
        String latestVersion = sortedVersions.get(sortedVersions.size() - 1);
        return versionMap.get(latestVersion);
    }
}
