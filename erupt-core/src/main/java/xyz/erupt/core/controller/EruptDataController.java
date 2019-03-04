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
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.annotation.sub_field.sub_edit.TabType;
import xyz.erupt.annotation.util.ConfigUtil;
import xyz.erupt.core.constant.RestPath;
import xyz.erupt.core.dao.EruptJapUtils;
import xyz.erupt.core.dao.EruptJpaDao;
import xyz.erupt.core.exception.EruptRuntimeException;
import xyz.erupt.core.model.*;
import xyz.erupt.core.service.InitService;
import xyz.erupt.core.service.DataService;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.SpringUtil;

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
    public Page getEruptData(@PathVariable("erupt") String eruptName,
                             @RequestBody JsonObject condition, HttpServletResponse response) {
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
            Page page = eruptJpaDao.queryEruptList(eruptModel, condition, new Page(pageIndex, pageSize, sort));
            for (Class<? extends DataProxy> proxy : eruptModel.getErupt().dateProxy()) {
                SpringUtil.getBean(proxy).afterFetch(page);
            }
            return page;
        } else {
            throw new EruptRuntimeException("没有查询权限");
        }
    }


    @PostMapping("/table/{erupt}/{id}/{subErupt}")
    @ResponseBody
    public Object getSubEruptData(@PathVariable("erupt") String eruptName,
                                  @PathVariable("id") String id,
                                  @PathVariable("subErupt") String subErupt) {
        EruptModel eruptModel = InitService.ERUPTS.get(eruptName);
        if (eruptModel.getErupt().power().query()) {
            //TODO 这个方法中的代码转移到JAP_DAO中
            EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(subErupt);
            TabType tabType = eruptFieldModel.getEruptField().edit().tabType()[0];
            EruptModel subEruptModel = InitService.ERUPTS.get(eruptFieldModel.getFieldReturnName());
            String subCondition = subEruptModel.getEruptName() + "." + subEruptModel.getErupt().primaryKeyCol() + "=" + id;
            if (!"".equals(tabType.filter().condition())) {
                subCondition += " and " + AnnotationUtil.switchFilterConditionToStr(tabType.filter());
            }
            String hql = EruptJapUtils.generateEruptJpaHql(eruptModel,
                    new HqlModel("new map(" + String.join(",", EruptJapUtils.getEruptColJapKeys(eruptModel)) + ")",
                            subCondition, null, tabType.sort()));

            return null;
        } else {
            throw new EruptRuntimeException("没有查询权限");
        }
    }

    @PostMapping("/tree/{erupt}/{id}/{subErupt}")
    @ResponseBody
    public Object getSubEruptTree(@PathVariable("erupt") String eruptName,
                                  @PathVariable("id") String id,
                                  @PathVariable("subErupt") String subErupt) {
        EruptModel eruptModel = InitService.ERUPTS.get(eruptName);
        if (eruptModel.getErupt().power().query()) {
            EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(subErupt);
            TabType tabType = eruptFieldModel.getEruptField().edit().tabType()[0];


            return null;
        } else {
            throw new EruptRuntimeException("没有查询权限");
        }
    }

    @PostMapping("/tree/{erupt}")
    @ResponseBody
    public List<TreeModel> getTreeEruptData(@PathVariable("erupt") String eruptName) {
        EruptModel eruptModel = InitService.ERUPTS.get(eruptName);
        if (eruptModel.getErupt().power().query()) {
            Tree tree = eruptModel.getErupt().tree();

            String[] cols = {
                    EruptJapUtils.compleHqlPath(eruptModel.getEruptName(), tree.id()) + " as " + tree.id().replace(".", "_"),
                    EruptJapUtils.compleHqlPath(eruptModel.getEruptName(), tree.label()) + " as " + tree.label().replace(".", "_"),
                    EruptJapUtils.compleHqlPath(eruptModel.getEruptName(), tree.pid()) + " as " + tree.pid().replace(".", "_")
            };
            List list = eruptJpaDao.getDataMap(eruptModel, cols);
            List<TreeModel> treeModels = new ArrayList<>();
            for (Object o : list) {
                Map<String, Object> map = (Map) o;
                TreeModel treeModel = new TreeModel(map.get(tree.id()), map.get(tree.label()), map.get(tree.pid().replace(".", "_")), null);
                treeModels.add(treeModel);
            }
            return EruptUtil.TreeModelToTree(treeModels);
        } else {
            throw new EruptRuntimeException("没有查询权限");
        }
    }

    @GetMapping("/{erupt}/{id}")
    @ResponseBody
    public EruptApiModel getEruptSingleData(@PathVariable("erupt") String eruptName, @PathVariable("id") String id) {
        EruptModel eruptModel = InitService.ERUPTS.get(eruptName);
        try {
            return EruptApiModel.successApi(eruptJpaDao.findDataById(eruptModel, id));
        } catch (Exception e) {
            return EruptApiModel.errorApi(e.getMessage());
        }
    }

    @PostMapping("/{erupt}/operator/{code}")
    @ResponseBody
    public EruptApiModel execEruptOperator(@PathVariable("erupt") String eruptName, @PathVariable("code") String code,
                                           @RequestBody JsonObject body) {
        EruptModel eruptModel = InitService.ERUPTS.get(eruptName);
        List<Object> oKeys = new ArrayList<>();
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
                eruptJpaDao.addEntity(eruptModel, obj);
                for (Class<? extends DataProxy> proxy : eruptModel.getErupt().dateProxy()) {
                    SpringUtil.getBean(proxy).afterAdd(obj);
                }
                return EruptApiModel.successApi(obj);
            } catch (Exception e) {
                return EruptApiModel.errorApi(e.getMessage());
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
                eruptJpaDao.editEntity(eruptModel, obj);
                for (Class<? extends DataProxy> proxy : eruptModel.getErupt().dateProxy()) {
                    SpringUtil.getBean(proxy).afterEdit(obj);
                }
                return EruptApiModel.successApi(null);
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
        EruptModel eruptModel = InitService.ERUPTS.get(erupt);
        if (eruptModel.getErupt().power().delete()) {
            try {
                Object obj = eruptJpaDao.findDataById(eruptModel, id);
                for (Class<? extends DataProxy> proxy : eruptModel.getErupt().dateProxy()) {
                    BoolAndReason boolAndReason = SpringUtil.getBean(proxy).beforeDelete(obj);
                    if (!boolAndReason.isBool()) {
                        return new EruptApiModel(boolAndReason);
                    }
                }
                eruptJpaDao.deleteEntity(obj);
                for (Class<? extends DataProxy> proxy : eruptModel.getErupt().dateProxy()) {
                    SpringUtil.getBean(proxy).afterDelete(obj);
                }
                return EruptApiModel.successApi(null);
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
            return EruptApiModel.errorApi(e.getMessage());
        }
        return eruptApiModel;
    }


    @GetMapping("/{erupt}/ref/{fieldName}")
    @ResponseBody
    public List getRefData(@PathVariable("erupt") String erupt, @PathVariable("fieldName") String fieldName) {
        EruptModel eruptModel = InitService.ERUPTS.get(erupt);
        return eruptJpaDao.getReferenceList(eruptModel, fieldName);
    }


}
