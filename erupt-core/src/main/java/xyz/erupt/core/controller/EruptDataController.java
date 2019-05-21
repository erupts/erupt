package xyz.erupt.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import xyz.erupt.core.exception.EruptNoLegalPowerException;
import xyz.erupt.core.model.*;
import xyz.erupt.core.service.CoreService;
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

    private static ObjectMapper MAPPER = new ObjectMapper();

    @PostMapping("/table/{erupt}")
    @ResponseBody
    public Page getEruptData(@PathVariable("erupt") String eruptName, @RequestBody JsonObject condition) {
        EruptModel eruptModel = CoreService.ERUPTS.get(eruptName);
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
                SpringUtil.getBean(proxy).afterFetch(page.getList());
            }
            return page;
        } else {
            throw new EruptNoLegalPowerException();
        }
    }

    @GetMapping("/tree/{erupt}")
    @ResponseBody
    public Collection<TreeModel> getEruptTreeData(@PathVariable("erupt") String eruptName) {
        EruptModel eruptModel = CoreService.ERUPTS.get(eruptName);
        if (eruptModel.getErupt().power().query()) {
            Collection<TreeModel> collection = AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz()).queryTree(eruptModel);
            for (Class<? extends DataProxy> proxy : eruptModel.getErupt().dateProxy()) {
                SpringUtil.getBean(proxy).afterFetch(collection);
            }
            return collection;
        } else {
            throw new EruptNoLegalPowerException();
        }
    }


    @GetMapping("/tab/table/{erupt}/{tabFieldName}")
    @ResponseBody
    public Object findTabList(@PathVariable("erupt") String eruptName, @PathVariable("tabFieldName") String tabFieldName) {
        tabFieldName = EruptUtil.handleNoRightVariable(tabFieldName);
        EruptModel eruptModel = CoreService.ERUPTS.get(eruptName);
        if (eruptModel.getErupt().power().viewDetails() || eruptModel.getErupt().power().edit()) {
            EruptFieldModel eruptFieldModel = CoreService.ERUPTS.get(eruptName).getEruptFieldMap().get(tabFieldName);
            return AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz()).findTabList(eruptFieldModel);
        } else {
            throw new EruptNoLegalPowerException();
        }
    }

    @GetMapping("/tab/table/{erupt}/{id}/{tabFieldName}")
    @ResponseBody
    public Object findTabListById(@PathVariable("erupt") String eruptName,
                                  @PathVariable("id") String id,
                                  @PathVariable("tabFieldName") String tabFieldName) {
        tabFieldName = EruptUtil.handleNoRightVariable(tabFieldName);
        EruptModel eruptModel = CoreService.ERUPTS.get(eruptName);
        if (eruptModel.getErupt().power().viewDetails() || eruptModel.getErupt().power().edit()) {
            return AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz()).findTabListById(eruptModel, tabFieldName, id);
        } else {
            throw new EruptNoLegalPowerException();
        }
    }

    @GetMapping("/tab/tree/{erupt}/{tabFieldName}")
    @ResponseBody
    public Object findTabTree(@PathVariable("erupt") String eruptName, @PathVariable("tabFieldName") String tabFieldName) {
        tabFieldName = EruptUtil.handleNoRightVariable(tabFieldName);
        EruptModel eruptModel = CoreService.ERUPTS.get(eruptName);
        EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(tabFieldName);
        if (eruptModel.getErupt().power().viewDetails() || eruptModel.getErupt().power().edit()) {
            return AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz()).findTabTree(eruptFieldModel);
        } else {
            throw new EruptNoLegalPowerException();
        }

    }

    @GetMapping("/tab/tree/{erupt}/{id}/{tabFieldName}")
    @ResponseBody
    public Object findTabTreeById(@PathVariable("erupt") String eruptName, @PathVariable("id") String id, @PathVariable("tabFieldName") String tabFieldName) {
        tabFieldName = EruptUtil.handleNoRightVariable(tabFieldName);
        EruptModel eruptModel = CoreService.ERUPTS.get(eruptName);
        if (eruptModel.getErupt().power().viewDetails() || eruptModel.getErupt().power().edit()) {
            return AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz()).findTabTreeById(eruptModel, tabFieldName, id);
        } else {
            throw new EruptNoLegalPowerException();
        }
    }

    @GetMapping("/{erupt}/{id}")
    @ResponseBody
    public EruptApiModel getEruptDataById(@PathVariable("erupt") String eruptName, @PathVariable("id") String id) {
        EruptModel eruptModel = CoreService.ERUPTS.get(eruptName);
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
        EruptModel eruptModel = CoreService.ERUPTS.get(eruptName);
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
        throw new EruptNoLegalPowerException();
    }

    @PostMapping("/{erupt}")
    @ResponseBody
    public EruptApiModel addEruptData(@PathVariable("erupt") String erupt, @RequestBody JsonObject data) {
        EruptModel eruptModel = CoreService.ERUPTS.get(erupt);
        BoolAndReason br = EruptUtil.eruptDataToViewData(eruptModel, data);
        if (!br.isBool()) {
            return new EruptApiModel(br);
        }
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
                return EruptApiModel.successApi(null);
            } catch (Exception e) {
                return EruptApiModel.errorApi(e);
            }
        } else {
            throw new EruptNoLegalPowerException();
        }
    }

    @PutMapping("/{erupt}")
    @ResponseBody
    public EruptApiModel editEruptData(@PathVariable("erupt") String erupt, @RequestBody JsonObject data) {
        EruptModel eruptModel = CoreService.ERUPTS.get(erupt);
        BoolAndReason br = EruptUtil.eruptDataToViewData(eruptModel, data);
        if (!br.isBool()) {
            return new EruptApiModel(br);
        }
        if (eruptModel.getErupt().power().edit()) {
            try {
                Object obj = this.gson.fromJson(data.toString(), eruptModel.getClazz());
//                Object obj = TypeUtil.mapToObject(data,eruptModel.getClazz());
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
            throw new EruptNoLegalPowerException();
        }
    }

    @DeleteMapping("/{erupt}/{id}")
    @ResponseBody
    public EruptApiModel deleteEruptData(@PathVariable("erupt") String erupt, @PathVariable("id") Serializable id) {
        EruptModel eruptModel = CoreService.ERUPTS.get(erupt);
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
            throw new EruptNoLegalPowerException();
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
        EruptModel eruptModel = CoreService.ERUPTS.get(erupt);
        return AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz()).getReferenceTree(eruptModel, fieldName);
    }

    @GetMapping("/{erupt}/reftree/{fieldName}/{dependValue}")
    @ResponseBody
    public Collection<TreeModel> getRefTreeDataByDepend(@PathVariable("erupt") String erupt, @PathVariable("fieldName") String fieldName, @PathVariable("dependValue") String dependValue) {
        EruptModel eruptModel = CoreService.ERUPTS.get(erupt);
        return AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz()).getReferenceTreeByDepend(eruptModel, fieldName, dependValue);
    }

//    @GetMapping("/{erupt}/reftable/{fieldName}")
//    @ResponseBody
//    public Collection getRefTableData(@PathVariable("erupt") String erupt, @PathVariable("fieldName") String fieldName) {
//        EruptModel eruptModel = InitService.ERUPTS.get(erupt);
//        return AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz()).getReferenceList(eruptModel, fieldName);
//    }


}
