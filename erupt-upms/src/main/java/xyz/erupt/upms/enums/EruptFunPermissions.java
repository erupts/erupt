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
    EDIT("修改"),
    DELETE("删除"),
    QUERY("查询"),
    EXPORT("导出"),
    IMPORTABLE("导入"),
    VIEW_DETAIL("查看详情");

    private final String name;

}
