package com.easysoftwareinput.domain.oepkg.model;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThreadPkgEntity {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPkgEntity.class);

    /**
     * pkgs to be stored.
     */
    private List<OePkg> pkgs;

    /**
     * pkg.
     */
    private FilePkgEntity fPkg;

    /**
     * start index.
     */
    private int start;

    /**
     * end index.
     */
    private int end;

    /**
     * create ThreadPkgEntity.
     * @param fPkg FilePkgEntity.
     * @param start start.
     * @param end end.
     * @param pkgs pkgs.
     * @return ThreadPkgEntity.
     */
    public static ThreadPkgEntity of(FilePkgEntity fPkg, int start, int end, List<OePkg> pkgs) {
        ThreadPkgEntity t = new ThreadPkgEntity();
        t.setFPkg(fPkg);
        t.setStart(start);
        t.setEnd(end);
        t.setPkgs(pkgs);
        return t;
    }

    /**
     * get file name.
     * @return file name.
     */
    public String getFileName() {
        if (fPkg != null) {
            return fPkg.getFileName();
        }
        return null;
    }

    /**
     * get os message.
     * @return OsMes.
     */
    public OsMes getOsMes() {
        if (fPkg != null) {
            return fPkg.getOsMes();
        }
        return null;
    }
}
