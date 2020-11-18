//package xyz.erupt.core.check;
//
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Service;
//import xyz.erupt.core.service.EruptApplication;
//
//@Order(0)
//@Service
//public class EruptStartupCheck implements ApplicationRunner {
//
//    @Override
//    public void run(ApplicationArguments args) {
//        if (EruptApplication.getScanPackage().length == 0) {
//            throw new RuntimeException("Not found scanner package place check `EruptApplication.run()` Whether to call");
//        }
//    }
//}
