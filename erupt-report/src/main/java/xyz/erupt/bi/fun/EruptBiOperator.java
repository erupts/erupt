//package xyz.erupt.bi.fun;
//
//import xyz.erupt.annotation.config.Comment;
//
//import java.util.List;
//import java.util.Map;
//
///**
// * @author YuePeng
// * date 2023/12/3 17:30
// */
//public interface EruptBiOperator {
//
//    @Comment("绑定 BI 报表编码")
//    String bindBi();
//
//    @Comment("功能名称")
//    String name();
//
//    @Comment("是否可用")
//    boolean enable();
//
//    @Comment("定义数据处理过程")
//    @Comment("返回值：自定义JS语句，支持null值")
//    String process(List<Map<String, Object>> data);
//
//
//}
