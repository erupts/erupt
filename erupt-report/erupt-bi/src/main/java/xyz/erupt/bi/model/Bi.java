package xyz.erupt.bi.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.annotation.sub_erupt.Drill;
import xyz.erupt.annotation.sub_erupt.Link;
import xyz.erupt.annotation.sub_erupt.LinkTree;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.bi.constant.BiConst;
import xyz.erupt.core.constant.MenuStatus;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.jpa.model.MetaModelUpdateVo;
import xyz.erupt.upms.model.EruptMenu;
import xyz.erupt.upms.service.EruptContextService;
import xyz.erupt.upms.service.EruptUserService;

import javax.annotation.Resource;
import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

/**
 * @author YuePeng
 * date 2019-08-26.
 */
@Entity
@Table(name = "e_bi", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Erupt(name = "报表配置",
        rowOperation = {
                @RowOperation(
                        title = "发布", mode = RowOperation.Mode.SINGLE, icon = "fa fa-send",
                        eruptClass = BiReleaseModal.class, operationHandler = Bi.class
                ),
//                @RowOperation(
//                        tip = "拷贝公开链接", title = "复制链接", mode = RowOperation.Mode.SINGLE, icon = "fa fa-link",
//                        operationHandler = CopyLinkHandler.class
//                ),
        },
        linkTree = @LinkTree(field = "biGroup"),
        dataProxy = BiDataProxy.class,
        drills = {
                @Drill(title = "图表配置", icon = "fa fa-pie-chart"
                        , link = @Link(linkErupt = BiChart.class, joinColumn = "bi.id")),
                @Drill(title = "修改记录", icon = "fa fa-history text-green"
                        , link = @Link(linkErupt = BiHistory.class, joinColumn = "bi.id"))
        })
@Getter
@Setter
@Component
@EruptI18n
public class Bi extends MetaModelUpdateVo implements OperationHandler<Bi, BiReleaseModal> {

    @EruptField(
            views = @View(title = "编码", width = "100px"),
            edit = @Edit(title = "编码", search = @Search, show = false)
    )
    private String code;

    @EruptField(
            views = @View(title = "名称", sortable = true),
            edit = @Edit(title = "名称", notNull = true, search = @Search(vague = true))
    )
    private String name;

    @ManyToOne
    @JoinColumn(name = "bi_group_id")
    @EruptField(
            views = @View(title = "组别", column = "name", sortable = true),
            edit = @Edit(title = "组别", notNull = true, type = EditType.REFERENCE_TREE,
                    referenceTreeType = @ReferenceTreeType(pid = "parent.id"), search = @Search)
    )
    private BiGroup biGroup;

    @ManyToOne
    @JoinColumn(name = "datasource_id")
    @EruptField(
            views = @View(title = "数据源", column = "name"),
            edit = @Edit(title = "数据源", type = EditType.REFERENCE_TREE, search = @Search)
    )
    private BiDataSource dataSource;

    @ManyToOne
    @EruptField(
            edit = @Edit(title = "处理类", type = EditType.REFERENCE_TABLE)
    )
    private BiClassHandler classHandler;

    @EruptField(
            views = @View(title = "缓存时间", sortable = true, template = "value&&value+'s'"),
            edit = @Edit(title = "缓存时间（秒）")
    )
    private Integer cacheTime = 1;

    @EruptField(
            views = @View(title = "自刷周期", sortable = true, template = "value&&value+'s'"),
            edit = @Edit(title = "自动刷新周期（秒）")
    )
    private Integer refreshTime;

    @EruptField(
            views = @View(title = "分页方式", sortable = true),
            edit = @Edit(title = "分页方式", notNull = true, type = EditType.CHOICE, choiceType = @ChoiceType(vl = {
                    @VL(value = BiConst.PAGE_END, label = "后端分页"),
                    @VL(value = BiConst.PAGE_FRONT, label = "前端分页"),
                    @VL(value = BiConst.PAGE_NONE, label = "不分页"),
            }))
    )
    private String pageType = BiConst.PAGE_END;

    @EruptField(
            views = @View(title = "导出", sortable = true),
            edit = @Edit(title = "导出", notNull = true, boolType = @BoolType(trueText = "开启", falseText = "关闭"))
    )
    private Boolean export = true;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @EruptField(
            views = {
                    @View(title = "SQL语句")
            },
            edit = @Edit(title = "SQL语句", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "sql"))
    )
    private String sqlStatement;

    @Transient
    @EruptField(
            views = @View(title = "效果预览", type = ViewType.LINK_DIALOG, desc = "需提前设置菜单权限")
    )
    private String view;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "bi_id")
    private Set<BiChart> biCharts;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "bi_id")
    @OrderBy("sort")
    @EruptField(
            edit = @Edit(title = "查询维度", type = EditType.TAB_TABLE_ADD)
    )
    private Set<BiDimension> biDimension;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "bi_id")
    @EruptField(
            edit = @Edit(title = "表格列", type = EditType.TAB_TABLE_ADD)
    )
    private Set<BiColumn> biColumns;

    @Resource
    @Transient
    private EruptDao eruptDao;

    @Resource
    @Transient
    private EruptUserService eruptUserService;

    @Resource
    @Transient
    private EruptContextService eruptContextService;

    @Override
    @Transactional
    public String exec(List<Bi> data, BiReleaseModal biReleaseModal, String[] param) {
        if (!eruptUserService.getSimpleUserInfo().isSuperAdmin()) {
            throw new EruptWebApiRuntimeException("报表发布请联系 '超级管理员' 操作！");
        }
        Bi bi = data.get(0);
        Erupts.requireNull(eruptDao.queryEntity(EruptMenu.class, String.format("code = '%s'", bi.getCode())),
                "菜单已存在请勿重复发布");
        Integer max = (Integer) eruptDao.getEntityManager()
                .createQuery("select max(sort) from " + EruptMenu.class.getSimpleName()).getSingleResult();
        EruptMenu eruptMenu = new EruptMenu(bi.getCode(), bi.getName(), BiConst.MENU_TYPE,
                bi.getCode(), MenuStatus.OPEN.getValue(), max + 10, null, biReleaseModal.getEruptMenu());
        eruptDao.persist(eruptMenu);
        //刷新当前用户菜单
        eruptUserService.cacheUserInfo(eruptUserService.getCurrentEruptUser(), eruptContextService.getCurrentToken());
        return null;
    }
}
