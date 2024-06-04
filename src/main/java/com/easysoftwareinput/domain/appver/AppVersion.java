package com.easysoftwareinput.domain.appver;

import lombok.Data;

@Data
public class AppVersion {
    /**
     * name of app.
     */
    private String name;

    /**
     * upstream homepage of app.
     */
    private String upHomepage;

    /**
     * openEuller homepage of app.
     */
    private String eulerHomepage;

    /**
     * backend of app.
     */
    private String backend;

    /**
     * version of upstream app.
     */
    private String upstreamVersion;

    /**
     * version of openEuler app.
     */
    private String openeulerVersion;

    /**
     * version of ci app.
     */
    private String ciVersion;

    /**
     * status of openEuler pkg.
     */
    private String status;

    /**
     * openEuler version.
     */
    private String eulerVersion;

    /**
     * version of openEuler os.
     */
    private String eulerOsVersion;
}
