package com.erupt.controller;

import com.erupt.annotation.fun.DataProxy;
import com.erupt.annotation.fun.OperationHandler;
import com.erupt.annotation.model.BoolAndReason;
import com.erupt.annotation.sub_erupt.RowOperation;
import com.erupt.annotation.sub_erupt.Tree;
import com.erupt.annotation.sub_erupt.TreeLoadType;
import com.erupt.constant.RestPath;
import com.erupt.dao.EruptJpaDao;
import com.erupt.base.model.EruptApiModel;
import com.erupt.base.model.HttpStatus;
import com.erupt.base.model.EruptModel;
import com.erupt.base.model.Page;
import com.erupt.base.model.TreeModel;
import com.erupt.exception.EruptRuntimeException;
import com.erupt.service.CoreService;
import com.erupt.service.DataService;
import com.erupt.util.EruptUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Erupt 对数据的增删改查
 * Created by liyuepeng on 9/28/18.
 */
@RestController
@RequestMapping(RestPath.ERUPT_DATA)
@Transactional
public class EruptDataController {

    @Autowired
    private DataService dataService;

    @Autowired
    private EruptJpaDao eruptJpaDao;

    private Gson gson = new Gson();

    public static final String PAGE_KEY = "page";


    @PostMapping("/table/{erupt}")
    @ResponseBody
    public Page getEruptData(@PathVariable("erupt") String eruptName, @RequestBody JsonObject data) throws JsonProcessingException, IllegalAccessException, InstantiationException {
        EruptModel eruptModel = CoreService.ERUPTS.get(eruptName);
        JsonObject conditionParam = data.getAsJsonObject(EruptJpaDao.CONDITION_KEY);
        JsonObject conditionPage = data.getAsJsonObject(PAGE_KEY);
        if (eruptModel.getErupt().power().query()) {
            DataProxy dataProxy = null;
            BoolAndReason boolAndReason = new BoolAndReason(true, null);
            if (eruptModel.getErupt().dateProxy().length > 0) {
                dataProxy = eruptModel.getErupt().dateProxy()[0].newInstance();
                //该参数为查询条件
                boolAndReason = dataProxy.beforeFetch(data);
            }
            if (boolAndReason.isBool()) {
                Page page = eruptJpaDao.queryEruptListByValidate(eruptModel, conditionParam,
                        new Page(
                                conditionPage.get(Page.PAGE_NUMBER_STR).getAsInt(),
                                conditionPage.get(Page.PAGE_SIZE_STR).getAsInt())
                );
                if (null != dataProxy) {
                    dataProxy.afterFetch(page.getList());
                }
                return page;
            }
            return null;
        } else {
            throw new EruptRuntimeException("没有查询权限");
        }
    }

    @PostMapping("/tree/{erupt}")
    @ResponseBody
    public List<TreeModel> getTreeEruptData(@PathVariable("erupt") String eruptName) {
        EruptModel eruptModel = CoreService.ERUPTS.get(eruptName);
        if (eruptModel.getErupt().power().query()) {
            Tree tree = eruptModel.getErupt().tree();
            List list;
            if (tree.loadType() == TreeLoadType.LAZY) {
                String cols[] = {
                        tree.id() + " as " + tree.id(),
                        tree.label() + " as " + tree.label(),
                        tree.pid() + " as " + tree.pid()
                };
                if (!"".equals(tree.icon())) {
                    cols[3] = tree.icon() + " as " + tree.icon();
                }
                list = eruptJpaDao.getDataMap(eruptModel, cols);
            } else {
                list = eruptJpaDao.queryEruptList(eruptModel, null, new Page(1, 9999)).getList();
            }

            List<TreeModel> treeModels = new ArrayList<>();
            for (Object o : list) {
                Map<String, Object> map = (Map) o;
                TreeModel treeModel = new TreeModel(map.get(tree.id()), map.get(tree.label()), map.get(tree.pid().replace(".", "_")), o);
                treeModels.add(treeModel);
            }
            return EruptUtil.TreeModelToTree(treeModels);
        } else {
            throw new EruptRuntimeException("没有查询权限");
        }
    }

    @PostMapping("/{erupt}/operator/{code}")
    @ResponseBody
    public BoolAndReason execEruptOperator(@PathVariable("erupt") String eruptName, @PathVariable("code") String code,
                                           @RequestBody JsonObject data) throws IllegalAccessException, InstantiationException {
        EruptModel eruptModel = CoreService.ERUPTS.get(eruptName);
        List<Object> oKeys = new ArrayList<>();
        JsonArray keys = data.getAsJsonArray("keys");
        JsonObject param = data.getAsJsonObject("param");
        for (RowOperation rowOperation : eruptModel.getErupt().rowOperation()) {
            if (code.equals(rowOperation.code())) {
                OperationHandler operationHandler = rowOperation.operationHandler().newInstance();
                return operationHandler.exec(keys, param);
            }
        }
        return new BoolAndReason(false, "找不到这个编码");
    }

    @PostMapping("/{erupt}")
    @ResponseBody
    public EruptApiModel addEruptData(@PathVariable("erupt") String erupt, @RequestBody Object data) throws IllegalAccessException, InstantiationException {
        EruptModel eruptModel = CoreService.ERUPTS.get(erupt);
        if (eruptModel.getErupt().power().add()) {
            Object obj = gson.fromJson(gson.toJson(data), eruptModel.getClazz());
            DataProxy dataProxy = null;
            BoolAndReason boolAndReason = new BoolAndReason(true, null);
            if (eruptModel.getErupt().dateProxy().length > 0) {
                dataProxy = eruptModel.getErupt().dateProxy()[0].newInstance();
                boolAndReason = dataProxy.beforeSave(obj);
            }
            if (boolAndReason.isBool()) {
                eruptJpaDao.saveEntity(eruptModel, obj);
                if (null != dataProxy) {
                    dataProxy.afterSave(obj);
                }
                return EruptApiModel.successApi(null);
            } else {
                return EruptApiModel.errorApi(boolAndReason.getReason());
            }
        } else {
            throw new EruptRuntimeException("没有新增权限");
        }
    }

    @PutMapping("/{erupt}/{id}")
    @ResponseBody
    public EruptApiModel editEruptData(@PathVariable("erupt") String erupt, @PathVariable("id") String id) throws IllegalAccessException, InstantiationException {
        EruptModel eruptModel = CoreService.ERUPTS.get(erupt);
        if (eruptModel.getErupt().power().add()) {
            DataProxy dataProxy = null;
            BoolAndReason boolAndReason = new BoolAndReason(true, null);
            if (eruptModel.getErupt().dateProxy().length > 0) {
                dataProxy = eruptModel.getErupt().dateProxy()[0].newInstance();
                boolAndReason = dataProxy.beforeSave(id);
            }
            if (boolAndReason.isBool()) {
                if (null != dataProxy) {
                    dataProxy.afterSave(null);
                }
                return EruptApiModel.successApi(null);
            } else {
                return EruptApiModel.errorApi(boolAndReason.getReason());
            }
        } else {
            throw new EruptRuntimeException("没有修改权限");
        }
    }

    @DeleteMapping("/{erupt}/{id}")
    @ResponseBody
    public EruptApiModel deleteEruptData(@PathVariable("erupt") String erupt, @PathVariable("id") Serializable id) throws IllegalAccessException, InstantiationException {
        EruptModel eruptModel = CoreService.ERUPTS.get(erupt);
        if (eruptModel.getErupt().power().add()) {
            DataProxy dataProxy = null;
            BoolAndReason boolAndReason = new BoolAndReason(true, null);
            Object obj = eruptJpaDao.findDataById(eruptModel, id);
            if (eruptModel.getErupt().dateProxy().length > 0) {
                dataProxy = eruptModel.getErupt().dateProxy()[0].newInstance();
                boolAndReason = dataProxy.beforeDelete(obj);
            }
            if (boolAndReason.isBool()) {
                eruptJpaDao.deleteEntity(obj);
                if (null != dataProxy) {
                    dataProxy.afterDelete(obj);
                }
                return EruptApiModel.successApi(null);
            } else {
                return EruptApiModel.errorApi(boolAndReason.getReason());
            }
        } else {
            throw new EruptRuntimeException("没有删除权限");
        }
    }

    //为了事务性考虑所以增加了批量删除功能
    @DeleteMapping("/{erupt}")
    @ResponseBody
    public EruptApiModel deleteEruptDatas(@PathVariable("erupt") String erupt, @RequestParam("ids") Serializable[] ids) throws IllegalAccessException, InstantiationException {
        EruptApiModel eruptApiModel = EruptApiModel.successApi(null);
        for (Serializable id : ids) {
            eruptApiModel = this.deleteEruptData(erupt, id);
            if (!eruptApiModel.isSuccess()) {
                break;
            }
        }
        return eruptApiModel;
    }


    @GetMapping("/{erupt}/ref/{name}")
    @ResponseBody
    public List getRefData(@PathVariable("erupt") String erupt, @PathVariable("name") String name) {
        EruptModel eruptModel = CoreService.ERUPTS.get(erupt);
        return eruptJpaDao.getReferenceList(eruptModel, name);
    }


}
