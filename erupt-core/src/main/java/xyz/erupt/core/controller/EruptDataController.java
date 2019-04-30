package xyz.erupt.core.controller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.annotation.model.BoolAndReason;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.exception.EruptRuntimeException;
import xyz.erupt.core.model.*;
import xyz.erupt.core.service.InitService;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.SpringUtil;

import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.Collection;

/**
 * Erupt 对数据的增删改查
 * Created by liyuepeng on 9/28/18.
 */
@RestController
@RequestMapping(RestPath.ERUPT_DATA)
public class EruptDataController {

    @Autowired
    private Gson gson;

    @PostMapping("/table/{erupt}")
    @ResponseBody
    public Page getEruptData(@PathVariable("erupt") String eruptName, @RequestBody JsonObject condition) {
        EruptModel eruptModel = InitService.ERUPTS.get(eruptName);
        int pageIndex = condition.get(Page.PAGE_INDEX_STR).getAsInt();
        int pageSize = condition.get(Page.PAGE_SIZE_STR).getAsInt();
        String sort = condition.get(Page.PAGE_SORT_STR).getAsString();
        condition.remove(Page.PAGE_INDEX_STR);
        condition.remove(Page.PAGE_SIZE_STR);
        condition.remove(Page.PAGE_SORT_STR);
        if (eruptModel.getErupt().power().query()) {
            for (Class<? extends DataProxy> proxy : eruptModel.getErupt().dateProxy()) {
                //该参数为查询条件
                SpringUtil.getBean(proxy).beforeFetch(condition);
            }
            Page page = AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz()).queryList(eruptModel, condition, new Page(pageIndex, pageSize, sort));
            for (Class<? extends DataProxy> proxy : eruptModel.getErupt().dateProxy()) {
                SpringUtil.getBean(proxy).afterFetch(page);
            }
            return page;
        } else {
            throw new EruptRuntimeException("没有查询权限");
        }
    }

    @RequestMapping("/tree/{erupt}")
    @ResponseBody
    public Collection<TreeModel> getEruptTreeData(@PathVariable("erupt") String eruptName) {
        EruptModel eruptModel = InitService.ERUPTS.get(eruptName);
        if (eruptModel.getErupt().power().query()) {
            return AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz()).queryTree(eruptModel);
        } else {
            throw new EruptRuntimeException("没有查询权限");
        }
    }


    @RequestMapping("/tab/table/{erupt}/{tabFieldName}")
    @ResponseBody
    public Object findTabList(@PathVariable("erupt") String eruptName, @PathVariable("tabFieldName") String tabFieldName) {
        tabFieldName = EruptUtil.handleNoRightVariable(tabFieldName);
        EruptModel eruptModel = InitService.ERUPTS.get(eruptName);
        EruptFieldModel eruptFieldModel = InitService.ERUPTS.get(eruptName).getEruptFieldMap().get(tabFieldName);
        return AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz()).findTabList(eruptFieldModel);
    }

    @RequestMapping("/tab/table/{erupt}/{id}/{tabFieldName}")
    @ResponseBody
    public Object findTabListById(@PathVariable("erupt") String eruptName,
                                  @PathVariable("id") String id,
                                  @PathVariable("tabFieldName") String tabFieldName) {
        tabFieldName = EruptUtil.handleNoRightVariable(tabFieldName);
        EruptModel eruptModel = InitService.ERUPTS.get(eruptName);
        return AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz()).findTabListById(eruptModel, tabFieldName, id);
    }

    @RequestMapping("/tab/tree/{erupt}/{tabFieldName}")
    @ResponseBody
    public Object findTabTree(@PathVariable("erupt") String eruptName, @PathVariable("tabFieldName") String tabFieldName) {
        tabFieldName = EruptUtil.handleNoRightVariable(tabFieldName);
        EruptModel eruptModel = InitService.ERUPTS.get(eruptName);
        EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(tabFieldName);
        return AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz()).findTabTree(eruptFieldModel);
    }

    @RequestMapping("/tab/tree/{erupt}/{id}/{tabFieldName}")
    @ResponseBody
    public Object findTabTreeById(@PathVariable("erupt") String eruptName, @PathVariable("id") String id, @PathVariable("tabFieldName") String tabFieldName) {
        tabFieldName = EruptUtil.handleNoRightVariable(tabFieldName);
        EruptModel eruptModel = InitService.ERUPTS.get(eruptName);
        return AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz()).findTabTreeById(eruptModel, tabFieldName, id);
    }

    @RequestMapping("/{erupt}/{id}")
    @ResponseBody
    public EruptApiModel getEruptDataById(@PathVariable("erupt") String eruptName, @PathVariable("id") String id) {
        EruptModel eruptModel = InitService.ERUPTS.get(eruptName);
        try {
            return EruptApiModel.successApi(AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz()).findDataById(eruptModel, id));
        } catch (Exception e) {
            return EruptApiModel.errorApi(e);
        }
    }

    @PostMapping("/{erupt}/operator/{code}")
    @ResponseBody
    public EruptApiModel execEruptOperator(@PathVariable("erupt") String eruptName, @PathVariable("code") String code,
                                           @RequestBody JsonObject body) {
        EruptModel eruptModel = InitService.ERUPTS.get(eruptName);
        for (RowOperation rowOperation : eruptModel.getErupt().rowOperation()) {
            if (code.equals(rowOperation.code())) {
                JsonElement param = body.get("param");
                if (param.isJsonNull()) {
                    param = null;
                }
                OperationHandler operationHandler = SpringUtil.getBean(rowOperation.operationHandler());
                return new EruptApiModel(operationHandler.exec(body.get("data"), param));
            }
        }
        return new EruptApiModel(new BoolAndReason(false, "Operate not found"));
    }

    @PostMapping("/{erupt}")
    @ResponseBody
    public EruptApiModel addEruptData(@PathVariable("erupt") String erupt, @RequestBody Object data) {
        EruptModel eruptModel = InitService.ERUPTS.get(erupt);
        if (eruptModel.getErupt().power().add()) {
            Object obj = gson.fromJson(gson.toJson(data), eruptModel.getClazz());
            try {
                for (Class<? extends DataProxy> proxy : eruptModel.getErupt().dateProxy()) {
                    BoolAndReason boolAndReason = SpringUtil.getBean(proxy).beforeAdd(obj);
                    if (!boolAndReason.isBool()) {
                        return new EruptApiModel(boolAndReason);
                    }
                }
                AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz()).addData(eruptModel, obj);
                for (Class<? extends DataProxy> proxy : eruptModel.getErupt().dateProxy()) {
                    SpringUtil.getBean(proxy).afterAdd(obj);
                }
                return EruptApiModel.successApi(obj);
            } catch (Exception e) {
                return EruptApiModel.errorApi(e);
            }
        } else {
            throw new EruptRuntimeException("没有新增权限");
        }
    }

    @PutMapping("/{erupt}")
    @ResponseBody
    public EruptApiModel editEruptData(@PathVariable("erupt") String erupt, @RequestBody Object data) {
        EruptModel eruptModel = InitService.ERUPTS.get(erupt);
        if (eruptModel.getErupt().power().add()) {
            try {
                Object obj = gson.fromJson(gson.toJson(data), eruptModel.getClazz());
                for (Class<? extends DataProxy> proxy : eruptModel.getErupt().dateProxy()) {
                    BoolAndReason boolAndReason = SpringUtil.getBean(proxy).beforeEdit(obj);
                    if (!boolAndReason.isBool()) {
                        return new EruptApiModel(boolAndReason);
                    }
                }
                AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz()).editData(eruptModel, obj);
                for (Class<? extends DataProxy> proxy : eruptModel.getErupt().dateProxy()) {
                    SpringUtil.getBean(proxy).afterEdit(obj);
                }
                return EruptApiModel.successApi(null);
            } catch (Exception e) {
                return EruptApiModel.errorApi(e);
            }
        } else {
            throw new EruptRuntimeException("没有修改权限");
        }
    }

    @DeleteMapping("/{erupt}/{id}")
    @ResponseBody
    public EruptApiModel deleteEruptData(@PathVariable("erupt") String erupt, @PathVariable("id") Serializable id) {
        EruptModel eruptModel = InitService.ERUPTS.get(erupt);
        if (eruptModel.getErupt().power().delete()) {
            try {
                for (Class<? extends DataProxy> proxy : eruptModel.getErupt().dateProxy()) {
                    BoolAndReason boolAndReason = SpringUtil.getBean(proxy).beforeDelete(id);
                    if (!boolAndReason.isBool()) {
                        return new EruptApiModel(boolAndReason);
                    }
                }
                AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz()).deleteData(eruptModel, id);
                for (Class<? extends DataProxy> proxy : eruptModel.getErupt().dateProxy()) {
                    SpringUtil.getBean(proxy).afterDelete(id);
                }
                return EruptApiModel.successApi(null);
            } catch (Exception e) {
                return EruptApiModel.errorApi(e);
            }
        } else {
            throw new EruptRuntimeException("没有删除权限");
        }
    }

    //为了事务性考虑所以增加了批量删除功能
    @Transactional
    @DeleteMapping("/{erupt}")
    @ResponseBody
    public EruptApiModel deleteEruptDatas(@PathVariable("erupt") String erupt, @RequestParam("ids") Serializable[] ids) {
        EruptApiModel eruptApiModel = EruptApiModel.successApi(null);
        try {
            for (Serializable id : ids) {
                eruptApiModel = this.deleteEruptData(erupt, id);
                if (!eruptApiModel.isSuccess()) {
                    break;
                }
            }
        } catch (Exception e) {
            return EruptApiModel.errorApi(e);
        }
        return eruptApiModel;
    }


    @GetMapping("/{erupt}/reftree/{fieldName}")
    @ResponseBody
    public Collection<TreeModel> getRefTreeData(@PathVariable("erupt") String erupt, @PathVariable("fieldName") String fieldName) {
        EruptModel eruptModel = InitService.ERUPTS.get(erupt);
        return AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz()).getReferenceTree(eruptModel, fieldName);
    }

    @GetMapping("/{erupt}/reftree/{fieldName}/{dependValue}")
    @ResponseBody
    public Collection<TreeModel> getRefTreeDataByDepend(@PathVariable("erupt") String erupt, @PathVariable("fieldName") String fieldName, @PathVariable("dependValue") String dependValue) {
        EruptModel eruptModel = InitService.ERUPTS.get(erupt);
        return AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz()).getReferenceTree(eruptModel, fieldName);
    }

//    @GetMapping("/{erupt}/reftable/{fieldName}")
//    @ResponseBody
//    public Collection getRefTableData(@PathVariable("erupt") String erupt, @PathVariable("fieldName") String fieldName) {
//        EruptModel eruptModel = InitService.ERUPTS.get(erupt);
//        return AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz()).getReferenceList(eruptModel, fieldName);
//    }


}
