package xyz.erupt.core.service;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.core.query.Aggregate;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author YuePeng
 * date 10/10/18.
 */
public interface IEruptDataService {

    @Comment("Define data source capabilities")
    default PowerObject power() {
        return new PowerObject();
    }

    @Comment("Retrieve data by primary key id")
    Object findDataById(EruptModel eruptModel, @Comment("Primary key value") Object id);

    @Comment("Query paginated data")
    Page queryList(EruptModel eruptModel, @Comment("Pagination object") Page page, @Comment("Conditions") EruptQuery eruptQuery);

    @Comment("Column totals over every row the conditions match, keyed by column; null when the source cannot aggregate")
    default Map<String, Object> aggregate(EruptModel eruptModel, @Comment("Conditions") EruptQuery eruptQuery, @Comment("Columns and their statistic") List<Aggregate> aggregates) {
        return null;
    }

    @Comment("Query data by columns")
    Collection<Map<String, Object>> queryColumn(EruptModel eruptModel, @Comment("Column information") List<Column> columns, @Comment("Conditions") EruptQuery eruptQuery);

    @Comment("Add data")
    void addData(EruptModel eruptModel, @Comment("Data object") Object object);

    @Comment("Edit data")
    void editData(EruptModel eruptModel, @Comment("Data object") Object object);

    @Comment("Delete data")
    void deleteData(EruptModel eruptModel, @Comment("Data object") Object object);

    @Comment("Batch insert")
    default void batchAddData(EruptModel eruptModel, @Comment("Data objects") List<?> objects) {
        for (Object o : objects) this.addData(eruptModel, o);
    }

    @Comment("Batch delete")
    default void batchDelete(EruptModel eruptModel, @Comment("Data objects") List<?> objects) {
        for (Object o : objects) this.deleteData(eruptModel, o);
    }

}
