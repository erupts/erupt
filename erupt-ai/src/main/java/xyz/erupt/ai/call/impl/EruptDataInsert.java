//package xyz.erupt.ai.call.impl;
//
//import jakarta.annotation.Resource;
//import jakarta.transaction.Transactional;
//import org.springframework.context.annotation.Scope;
//import org.springframework.stereotype.Component;
//import xyz.erupt.ai.annotation.AiParam;
//import xyz.erupt.ai.call.AiFunctionCall;
//import xyz.erupt.core.config.GsonFactory;
//import xyz.erupt.core.service.EruptCoreService;
//import xyz.erupt.core.view.EruptModel;
//import xyz.erupt.jpa.dao.EruptDao;
//
///**
// * @author YuePeng
// * date 2025/3/14 23:25
// */
//@Component
//@Scope("prototype")
//public class EruptDataInsert implements AiFunctionCall {
//
//    @AiParam(description = "Erupt Name")
//    private String eruptName;
//
//    @AiParam(description = "Data")
//    private String data;
//
//    @Resource
//    private EruptDao eruptDao;
//
//    @Override
//    public String description() {
//        return "Insert Erupt data";
//    }
//
//    @Override
//    public String call(String prompt) {
//        EruptModel erupt = EruptCoreService.getErupt(eruptName);
//        Object d = GsonFactory.getGson().fromJson(data, erupt.getClazz());
//        try {
//            eruptDao.persist(d);
//            return "Insert success";
//        } catch (Exception e) {
//            e.printStackTrace();
//            return e.getMessage();
//        }
//    }
//}
