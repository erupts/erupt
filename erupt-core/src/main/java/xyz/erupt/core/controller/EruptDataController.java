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
import xyz.erupt.annotation.sub_field.sub_edit.TabType;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.dao.EruptJapUtils;
import xyz.erupt.core.dao.EruptJpaDao;
import xyz.erupt.core.exception.EruptRuntimeException;
import xyz.erupt.core.model.*;
import xyz.erupt.core.service.DBService;
import xyz.erupt.core.service.InitService;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.SpringUtil;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.List;

/**
 * Erupt 对数据的增删改查
 * Created by liyuepeng on 9/28/18.
 */
@RestController
@RequestMapping(RestPath.ERUPT_DATA)
public class EruptDataController {

    @Autowired
    private DBService dbService;

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
            Page page = dbService.queryList(eruptModel, condition, new Page(pageIndex, pageSize, sort));
            for (Class<? extends DataProxy> proxy : eruptModel.getErupt().dateProxy()) {
                SpringUtil.getBean(proxy).afterFetch(page);
            }
            return page;
        } else {
            throw new EruptRuntimeException("没有查询权限");
        }
    }


    @RequestMapping("/table/{erupt}/{tabFieldName}")
    @ResponseBody
    public Object findTabList(@PathVariable("erupt") String eruptName, @PathVariable("tabFieldName") String tabFieldName) {
        tabFieldName = EruptUtil.handleNoRightVariable(tabFieldName);
        EruptFieldModel eruptFieldModel = InitService.ERUPTS.get(eruptName).getEruptFieldMap().get(tabFieldName);
        return dbService.findTabList(eruptFieldModel);
    }

    @RequestMapping("/table/{erupt}/{id}/{tabFieldName}")
    @ResponseBody
    public Object findTabListById(@PathVariable("erupt") String eruptName,
                                  @PathVariable("id") String id,
                                  @PathVariable("tabFieldName") String tabFieldName) {
        tabFieldName = EruptUtil.handleNoRightVariable(tabFieldName);
        EruptModel eruptModel = InitService.ERUPTS.get(eruptName);
        return dbService.findTabListById(eruptModel, tabFieldName, id);
    }

    @RequestMapping("/tree/{erupt}/{tabFieldName}")
    @ResponseBody
    public Object findTabTree(@PathVariable("erupt") String eruptName, @PathVariable("tabFieldName") String tabFieldName) {
        tabFieldName = EruptUtil.handleNoRightVariable(tabFieldName);
        EruptModel eruptModel = InitService.ERUPTS.get(eruptName);
        EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(tabFieldName);
        return dbService.findTabTree(eruptFieldModel);
    }

    @RequestMapping("/tree/{erupt}/{id}/{tabFieldName}")
    @ResponseBody
    public Object findTabTreeById(@PathVariable("erupt") String eruptName, @PathVariable("id") String id, @PathVariable("tabFieldName") String tabFieldName) {
        tabFieldName = EruptUtil.handleNoRightVariable(tabFieldName);
        EruptModel eruptModel = InitService.ERUPTS.get(eruptName);
        return dbService.findTabTreeById(eruptModel, tabFieldName, id);
    }

    @RequestMapping("/tree/{erupt}")
    @ResponseBody
    public List<TreeModel> getTreeEruptData(@PathVariable("erupt") String eruptName) {
        EruptModel eruptModel = InitService.ERUPTS.get(eruptName);
        if (eruptModel.getErupt().power().query()) {
            return dbService.queryTree(eruptModel);
        } else {
            throw new EruptRuntimeException("没有查询权限");
        }
    }

    @RequestMapping("/{erupt}/{id}")
    @ResponseBody
    public EruptApiModel getEruptDataById(@PathVariable("erupt") String eruptName, @PathVariable("id") String id) {
        EruptModel eruptModel = InitService.ERUPTS.get(eruptName);
        try {
            return EruptApiModel.successApi(dbService.findDataById(eruptModel, id));
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
        return new EruptApiModel(new BoolAndReason(false, "功能不存在"));
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
                dbService.addData(eruptModel, obj);
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
                dbService.editData(eruptModel, obj);
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
                dbService.deleteData(eruptModel, id);
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


    @GetMapping("/{erupt}/ref/{fieldName}")
    @ResponseBody
    public List getRefData(@PathVariable("erupt") String erupt, @PathVariable("fieldName") String fieldName) {
        EruptModel eruptModel = InitService.ERUPTS.get(erupt);
        return dbService.getReferenceList(eruptModel, fieldName);
    }


}
