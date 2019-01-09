package xyz.erupt.controller;

import com.google.gson.JsonElement;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.annotation.model.BoolAndReason;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.annotation.sub_erupt.TreeLoadType;
import xyz.erupt.base.model.EruptApiModel;
import xyz.erupt.base.model.EruptModel;
import xyz.erupt.base.model.Page;
import xyz.erupt.base.model.TreeModel;
import xyz.erupt.constant.RestPath;
import xyz.erupt.dao.EruptJapUtils;
import xyz.erupt.dao.EruptJpaDao;
import xyz.erupt.exception.EruptRuntimeException;
import xyz.erupt.service.CoreService;
import xyz.erupt.service.DataService;
import xyz.erupt.util.EruptUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public Page getEruptData(@PathVariable("erupt") String eruptName,
                             @RequestBody JsonObject condition) throws JsonProcessingException, IllegalAccessException, InstantiationException {
        EruptModel eruptModel = CoreService.ERUPTS.get(eruptName);
        int pageIndex = condition.get(Page.PAGE_INDEX_STR).getAsInt();
        int pageSize = condition.get(Page.PAGE_SIZE_STR).getAsInt();
        String sort = condition.get(Page.PAGE_SORT_STR).getAsString();
        condition.remove(Page.PAGE_INDEX_STR);
        condition.remove(Page.PAGE_SIZE_STR);
        condition.remove(Page.PAGE_SORT_STR);
        if (eruptModel.getErupt().power().query()) {
            DataProxy dataProxy = null;
            BoolAndReason boolAndReason = new BoolAndReason(true, null);
            if (eruptModel.getErupt().dateProxy().length > 0) {
                dataProxy = eruptModel.getErupt().dateProxy()[0].newInstance();
                //该参数为查询条件
                boolAndReason = dataProxy.beforeFetch(condition);
            }
            if (boolAndReason.isBool()) {
                Page page = eruptJpaDao.queryEruptListByValidate(eruptModel, condition, new Page(pageIndex, pageSize, sort));
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
                list = eruptJpaDao.queryEruptList(eruptModel, null, new Page(1, 9999, null)).getList();
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
    public EruptApiModel execEruptOperator(@PathVariable("erupt") String eruptName, @PathVariable("code") String code,
                                           @RequestBody JsonObject body) throws IllegalAccessException, InstantiationException {
        EruptModel eruptModel = CoreService.ERUPTS.get(eruptName);
        List<Object> oKeys = new ArrayList<>();
        for (RowOperation rowOperation : eruptModel.getErupt().rowOperation()) {
            if (code.equals(rowOperation.code())) {
                JsonElement param = body.get("param");
                if (param.isJsonNull()) {
                    param = null;
                }
                OperationHandler operationHandler = rowOperation.operationHandler().newInstance();
                return new EruptApiModel(operationHandler.exec(body.get("data"), param));
            }
        }
        return new EruptApiModel(new BoolAndReason(false, "操作不存在"));
    }

    @PostMapping("/{erupt}")
    @ResponseBody
    public EruptApiModel addEruptData(@PathVariable("erupt") String erupt, @RequestBody Object data) throws IllegalAccessException, InstantiationException {
        EruptModel eruptModel = CoreService.ERUPTS.get(erupt);
        if (eruptModel.getErupt().power().add()) {
            Object obj = gson.fromJson(gson.toJson(data), eruptModel.getClazz());
            DataProxy dataProxy = null;
            BoolAndReason boolAndReason = new BoolAndReason(true, null);
            try {
                if (eruptModel.getErupt().dateProxy().length > 0) {
                    dataProxy = eruptModel.getErupt().dateProxy()[0].newInstance();
                    boolAndReason = dataProxy.beforeAdd(obj);
                }
                if (boolAndReason.isBool()) {
                    eruptJpaDao.saveEntity(eruptModel, obj);
//                try{
//                    eruptJpaDao.saveEntity(eruptModel, obj);
//                }catch (SQLIntegrityConstraintViolationException e){
//
//                }
                    if (null != dataProxy) {
                        dataProxy.afterAdd(obj);
                    }
                    return EruptApiModel.successApi(null);
                } else {
                    return EruptApiModel.errorApi(boolAndReason.getReason());
                }
            } catch (Exception e) {
                return EruptApiModel.errorApi(e.getMessage());
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
            try {
                DataProxy dataProxy = null;
                BoolAndReason boolAndReason = new BoolAndReason(true, null);
                if (eruptModel.getErupt().dateProxy().length > 0) {
                    dataProxy = eruptModel.getErupt().dateProxy()[0].newInstance();
                    boolAndReason = dataProxy.beforeEdit(id);
                }
                if (boolAndReason.isBool()) {
                    if (null != dataProxy) {
                        dataProxy.afterEdit(null);
                    }
                    return EruptApiModel.successApi(null);
                } else {
                    return EruptApiModel.errorApi(boolAndReason.getReason());
                }

            } catch (Exception e) {
                return EruptApiModel.errorApi(e.getMessage());
            }
        } else {
            throw new EruptRuntimeException("没有修改权限");
        }
    }

    @DeleteMapping("/{erupt}/{id}")
    @ResponseBody
    public EruptApiModel deleteEruptData(@PathVariable("erupt") String erupt, @PathVariable("id") Serializable id) {
        EruptModel eruptModel = CoreService.ERUPTS.get(erupt);
        if (eruptModel.getErupt().power().delete()) {
            DataProxy dataProxy = null;
            BoolAndReason boolAndReason = new BoolAndReason(true, null);
            try {
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
            } catch (Exception e) {
                return EruptApiModel.errorApi(e.getMessage());
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
        try {
            for (Serializable id : ids) {
                eruptApiModel = this.deleteEruptData(erupt, id);
                if (!eruptApiModel.isSuccess()) {
                    break;
                }
            }
        } catch (Exception e) {
            return EruptApiModel.errorApi(e.getMessage());
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
