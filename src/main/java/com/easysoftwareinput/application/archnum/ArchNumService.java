package com.easysoftwareinput.application.archnum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.easysoftwareinput.application.oepkg.IService;
import com.easysoftwareinput.domain.archnum.model.ArchNumContext;
import com.easysoftwareinput.domain.oepkg.model.OePkgEntity;
import com.easysoftwareinput.infrastructure.apppkg.AppGatewayImpl;
import com.easysoftwareinput.infrastructure.archnum.ArchNumGatewayImpl;
import com.easysoftwareinput.infrastructure.archnum.OsArchNumDO;
import com.easysoftwareinput.infrastructure.epkgpkg.EpkgGatewayImpl;
import com.easysoftwareinput.infrastructure.fieldpkg.FieldGatewayImpl;
import com.easysoftwareinput.infrastructure.oepkg.OepkgGatewayImpl;
import com.easysoftwareinput.infrastructure.rpmpkg.Gateway;
import com.easysoftwareinput.infrastructure.rpmpkg.RpmGatewayImpl;

@Component
public class ArchNumService implements IService {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ArchNumService.class);

    /**
     * RpmGatewayImpl.
     */
    @Autowired
    private RpmGatewayImpl rpmGateway;

    /**
     * AppGatewayImpl.
     */
    @Autowired
    private AppGatewayImpl appGateway;

    /**
     * EpkgGatewayImpl.
     */
    @Autowired
    private EpkgGatewayImpl epkgGateway;

    /**
     * FieldGatewayImpl.
     */
    @Autowired
    private FieldGatewayImpl fieldGateway;

    /**
     * OepkgGatewayImpl.
     */
    @Autowired
    private OepkgGatewayImpl oepkgGateway;

    /**
     * ArchNumGatewayImpl.
     */
    @Autowired
    private ArchNumGatewayImpl archNumGateway;

    /**
     * context.
     */
    private OePkgEntity context;

    /**
     * init context.
     * @return context.
     */
    public ArchNumContext initContext() {
        ArchNumContext context = new ArchNumContext();
        context.setStartTime(System.currentTimeMillis());

        List<OsArchNumDO> list = archNumGateway.getExistedIds("pkg_id");
        Set<String> sets = list.stream().map(OsArchNumDO::getPkgId).collect(Collectors.toSet());
        context.setExistedPkgIds(sets);
        return context;
    }

    /**
     * run the program.
     */
    public void run() {
        this.context = initContext();
        List<OsArchNumDO> pkgs = getOsArchNum();
        pkgs = filterpkgs(pkgs);
        if (pkgs == null || pkgs.isEmpty()) {
            return;
        }

        this.context.setCount(pkgs.size());
        archNumGateway.saveAll(this.context, pkgs);

        validData();
        LOGGER.info("finish-archnum");
    }

    /**
     * filter the OsArchNumDO.
     * @param list origin list of OsArchNumDO.
     * @return list of OsArchNumDO.
     */
    public List<OsArchNumDO> filterpkgs(List<OsArchNumDO> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> allowedArches = List.of("aarch64", "noarch", "x86_64", "loongarch64",
        "riscv64", "sw_64");
        return list.stream().filter(pkg -> allowedArches.contains(pkg.getArchName())).collect(Collectors.toList());
    }

    /**
     * get OsArchNumDO from tables.
     * @return OsArchNumDO.
     */
    public List<OsArchNumDO> getOsArchNum() {
        List<CompletableFuture<List<OsArchNumDO>>> tasks = new ArrayList<>();
        tasks.add(CompletableFuture.supplyAsync(() -> {
            return rpmGateway.getOsArchNum();
        }));
        tasks.add(CompletableFuture.supplyAsync(() -> {
            return appGateway.getOsArchNum();
        }));
        tasks.add(CompletableFuture.supplyAsync(() -> {
            return epkgGateway.getOsArchNum();
        }));
        tasks.add(CompletableFuture.supplyAsync(() -> {
            return fieldGateway.getOsArchNum();
        }));
        tasks.add(CompletableFuture.supplyAsync(() -> {
            return oepkgGateway.getOsArchNum();
        }));

        List<OsArchNumDO> res = new ArrayList<>();
        for (CompletableFuture<List<OsArchNumDO>> task : tasks) {
            try {
                res.addAll(task.get());
            } catch (Exception e) {
                LOGGER.error("cannot get osarch, e: {}", e.getMessage());
            }
        }
        return res;
    }

    /**
     * get gateway.
     */
    @Override
    public Gateway<OsArchNumDO> getCurrentGateway() {
        return this.archNumGateway;
    }

    /**
     * get start time.
     */
    @Override
    public long getStartTime() {
        return this.context.getStartTime();
    }

    /**
     * get count.
     */
    @Override
    public long getCount() {
        return this.context.getCount();
    }

    /**
     * get logger.
     */
    @Override
    public Logger getLogger() {
        return ArchNumService.LOGGER;
    }
}
