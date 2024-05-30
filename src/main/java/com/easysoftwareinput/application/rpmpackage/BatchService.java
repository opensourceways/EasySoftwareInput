package com.easysoftwareinput.application.rpmpackage;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easysoftwareinput.infrastructure.BasePackageDO;


public interface BatchService extends IService<BasePackageDO>{
    void saveOrUpdateBatchData(List<BasePackageDO> dataObjects);
}
