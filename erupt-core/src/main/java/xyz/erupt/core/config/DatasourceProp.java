//package xyz.erupt.core.config;
//
//import lombok.Data;
//import lombok.Getter;
//import lombok.Setter;
//import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
//import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
///**
// * @author liyuepeng
// * @date 2020-08-16
// */
//@Data
//@Component
//@ConfigurationProperties(prefix = "erupt.multi-db")
//public class DatasourceProp {
//
//    private List<DB> list;
//
//    @Getter
//    @Setter
//    public static class DB {
//
//        private DataSourceProperties datasource;
//
//        private JpaProperties jpa;
//
//    }
//}
