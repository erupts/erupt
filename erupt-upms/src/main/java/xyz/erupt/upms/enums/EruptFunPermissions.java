package xyz.erupt.upms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author YuePeng
 * date 2022/1/14 21:30
 */
@AllArgsConstructor
@Getter
public enum EruptFunPermissions {

    ADD("新增"),
    DELETE("删除"),
    EDIT("修改"),
    QUERY("查询"),
    EXPORT("导出"),
    IMPORT("导入"),
    VIEW_DETAIL("查看详情");

    private String name;

}
