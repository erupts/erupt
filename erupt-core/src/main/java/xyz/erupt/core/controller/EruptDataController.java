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
import xyz.erupt.core.annotation.EruptRouter;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.exception.EruptNoLegalPowerException;
import xyz.erupt.core.model.*;
import xyz.erupt.core.service.CoreService;
import xyz.erupt.core.util.*;

import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

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
    @EruptRouter
    @ResponseBody
    public Page getEruptData(@PathVariable("erupt") String eruptName, @RequestBody JsonObject searchCondition) {
        return this.getEruptData(eruptName,
                searchCondition.remove(Page.PAGE_INDEX_STR).getAsInt(),
                searchCondition.remove(Page.PAGE_SIZE_STR).getAsInt(),
                searchCondition.remove(Page.PAGE_SORT_STR).getAsString(), searchCondition);
    }

    public Page getEruptData(String eruptName, int pageIndex, int pageSize, String sort, JsonObject searchCondition) {
        EruptModel eruptModel = CoreService.getErupt(eruptName);
        if (eruptModel.getErupt().power().query()) {
            String customerCondition = null;
            for (Class<? extends DataProxy> proxy : eruptModel.getErupt().dateProxy()) {
                customerCondition = SpringUtil.getBean(proxy).beforeFetch(searchCondition);
            }
            Page page = AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz()).queryList(eruptModel, new Page(pageIndex, pageSize, sort), searchCondition, customerCondition);
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
    @EruptRouter
    public Collection<TreeModel> getEruptTreeData(@PathVariable("erupt") String eruptName) {
        EruptModel eruptModel = CoreService.getErupt(eruptName);
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

    @GetMapping("/{erupt}/{id}")
    @ResponseBody
    @EruptRouter
    public Map<String, Object> getEruptDataById(@PathVariable("erupt") String eruptName, @PathVariable("id") String id) {
        EruptModel eruptModel = CoreService.getErupt(eruptName);
        if (eruptModel.getErupt().power().edit() || eruptModel.getErupt().power().viewDetails()) {
            Object data = AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz()).findDataById(eruptModel, id);
            return EruptUtil.generateEruptDataMap(eruptModel, data);
        } else {
            throw new EruptNoLegalPowerException();
        }
    }

    @PostMapping("/{erupt}")
    @ResponseBody
    @EruptRouter
    public EruptApiModel addEruptData(@PathVariable("erupt") String erupt, @RequestBody JsonObject data) {
        EruptModel eruptModel = CoreService.getErupt(erupt);
//        BoolAndReason br = EruptUtil.validateEruptNotNull(eruptModel, data);
//        if (!br.isBool()) {
//            return new EruptApiModel(br);
//        }
        if (eruptModel.getErupt().power().add()) {
            Object obj = gson.fromJson(gson.toJson(data), eruptModel.getClazz());
            DataHandlerUtil.clearObjectDefaultValueByJson(obj, data);
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
    @EruptRouter
    public EruptApiModel editEruptData(@PathVariable("erupt") String erupt, @RequestBody JsonObject data) {
        EruptModel eruptModel = CoreService.getErupt(erupt);
//        BoolAndReason br = EruptValidate.validate(eruptModel,data);
//        if (!br.isBool()) {
//            return new EruptApiModel(br);
//        }
        if (eruptModel.getErupt().power().edit()) {
            try {
                Object obj = this.gson.fromJson(data.toString(), eruptModel.getClazz());
                DataHandlerUtil.clearObjectDefaultValueByJson(obj, data);
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
    @EruptRouter
    public EruptApiModel deleteEruptData(@PathVariable("erupt") String erupt, @PathVariable("id") Serializable id) {
        EruptModel eruptModel = CoreService.getErupt(erupt);
        if (eruptModel.getErupt().power().delete()) {
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
        } else {
            throw new EruptNoLegalPowerException();
        }
    }

    //为了事务性考虑所以增加了批量删除功能
    @Transactional
    @DeleteMapping("/{erupt}")
    @ResponseBody
    @EruptRouter
    public EruptApiModel deleteEruptDataList(@PathVariable("erupt") String erupt, @RequestParam("ids") Serializable[] ids) {
        EruptApiModel eruptApiModel = EruptApiModel.successApi(null);
        for (Serializable id : ids) {
            eruptApiModel = this.deleteEruptData(erupt, id);
            if (!eruptApiModel.isSuccess()) {
                break;
            }
        }
        return eruptApiModel;
    }

    @PostMapping("/{erupt}/operator/{code}")
    @ResponseBody
    @EruptRouter
    public EruptApiModel execEruptOperator(@PathVariable("erupt") String eruptName, @PathVariable("code") String code,
                                           @RequestBody JsonObject body) {
        EruptModel eruptModel = CoreService.getErupt(eruptName);
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

    @GetMapping("/tab/tree/{erupt}/{tabFieldName}")
    @ResponseBody
    @EruptRouter
    public Collection<TreeModel> findTabTree(@PathVariable("erupt") String eruptName, @PathVariable("tabFieldName") String tabFieldName) {
        tabFieldName = EruptUtil.handleNoRightVariable(tabFieldName);
        EruptModel eruptModel = CoreService.getErupt(eruptName);
        if (eruptModel.getErupt().power().viewDetails() || eruptModel.getErupt().power().edit()) {
            return AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz()).findTabTree(eruptModel, tabFieldName);
        } else {
            throw new EruptNoLegalPowerException();
        }
    }

    @PostMapping("/{erupt}/refer-table/{fieldName}")
    @ResponseBody
    @EruptRouter
    public Page getReferTable(@PathVariable("erupt") String eruptName, @PathVariable("fieldName") String fieldName,
                              @RequestBody JsonObject searchCondition) {
//        fieldName = EruptUtil.handleNoRightVariable(fieldName);
        EruptModel eruptModel = CoreService.getErupt(eruptName);
        if (eruptModel.getErupt().power().edit()) {
            int pageIndex = searchCondition.remove(Page.PAGE_INDEX_STR).getAsInt();
            int pageSize = searchCondition.remove(Page.PAGE_SIZE_STR).getAsInt();
            String sort = searchCondition.remove(Page.PAGE_SORT_STR).getAsString();
            EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(fieldName);
            EruptModel eruptReferenceModel = CoreService.getErupt(eruptFieldModel.getFieldReturnName());
            return AnnotationUtil.getEruptDataProcessor(eruptReferenceModel.getClazz()).queryList(
                    eruptReferenceModel, new Page(pageIndex, pageSize, sort),
                    searchCondition,
                    AnnotationUtil.switchFilterConditionToStr(eruptFieldModel.getEruptField().edit().filter()));
        } else {
            throw new EruptNoLegalPowerException();
        }
    }

    // REFERENCE API
    @GetMapping("/{erupt}/reference-tree/{fieldName}")
    @ResponseBody
    @EruptRouter
    public Collection<TreeModel> getReferenceTree(@PathVariable("erupt") String erupt, @PathVariable("fieldName") String fieldName) {
        EruptModel eruptModel = CoreService.getErupt(erupt);
        return AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz()).getReferenceTree(eruptModel, fieldName);
    }


    @GetMapping("/{erupt}/reference-tree/{fieldName}/{dependValue}")
    @ResponseBody
    @EruptRouter
    public Collection<TreeModel> getReferenceTreeByDepend(@PathVariable("erupt") String erupt, @PathVariable("fieldName") String fieldName, @PathVariable("dependValue") Serializable dependValue) {
        EruptModel eruptModel = CoreService.getErupt(erupt);
        EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(fieldName);
        //TODO 代码过长，应该做优化
        return AnnotationUtil.getEruptDataProcessor(eruptModel.getClazz()).getReferenceTreeByDepend(eruptModel, fieldName,
                TypeUtil.typeStrConvertObject(dependValue,
                        CoreService.getErupt(eruptFieldModel.getFieldReturnName()).getEruptFieldMap().get(eruptFieldModel.getEruptField().
                                edit().referenceTreeType().dependColumn()).getField().getType().getSimpleName()));
    }
    // REFERENCE API END

    @PostMapping("/validate-erupt/{erupt}")
    @ResponseBody
    @EruptRouter
    public EruptApiModel validateErupt(@PathVariable("erupt") String erupt, @RequestBody JsonObject data) {
        EruptModel eruptModel = CoreService.getErupt(erupt);

        return new EruptApiModel(true, null, null);
    }


}
