package com.easysoftwareinput.domain.appver;

import lombok.Data;

@Data
public class AppVersion {
    public String name;
    public String upHomepage;
    public String eulerHomepage;
    public String backend;
    public String upstreamVersion;
    public String openeulerVersion;
    public String ciVersion;
    public String status;
    public String eulerVersion;
    public String eulerOsVersion;
}
