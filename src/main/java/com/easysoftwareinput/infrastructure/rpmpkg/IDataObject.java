package com.easysoftwareinput.infrastructure.rpmpkg;

public interface IDataObject {
    /**
     * get os.
     * @return os.
     */
    String getOs();

    /**
     * get arch.
     * @return arch.
     */
    String getArch();

    /**
     * get count.
     * @return count.
     */
    Integer getCount();

    /**
     * get name.
     * @return name.
     */
    String getName();

    /**
     * get version.
     * @return version.
     */
    String getVersion();

    /**
     * get appver.
     * @return appver.
     */
    String getAppVer();

    /**
     * get pkg id.
     * @return pkg id.
     */
    String getPkgId();

    /**
     * get maintainer id.
     * @return maintainer id.
     */
    String getMaintainerId();

    /**
     * get category.
     * @return category.
     */
    String getCategory();

    /**
     * get description.
     * @return description.
     */
    String getDescription();

    /**
     * get icon url.
     * @return icon url.
     */
    String getIconUrl();
}
