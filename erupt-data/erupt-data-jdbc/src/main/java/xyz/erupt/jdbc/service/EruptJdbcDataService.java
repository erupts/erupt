package xyz.erupt.jdbc.service;

import jakarta.annotation.Resource;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.IEruptDataService;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.jdbc.annotation.EruptJdbc;
import xyz.erupt.jdbc.support.JdbcModelTable;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plain JDBC data source: models annotated with {@link EruptJdbc} are mapped to a single
 * table on the configured DataSource. All SQL generation lives in {@link JdbcModelTable}.
 *
 * @author YuePeng
 */
@Service
public class EruptJdbcDataService implements IEruptDataService {

    public static final String DATA_PROCESSOR = "JDBC";

    static {
        DataProcessorManager.register(DATA_PROCESSOR, EruptJdbcDataService.class);
    }

    @Resource
    private ApplicationContext applicationContext;

    private final Map<String, NamedParameterJdbcTemplate> templates = new ConcurrentHashMap<>();

    private final JdbcModelTable table = new JdbcModelTable(
            model -> this.template(this.eruptJdbc(model)), model -> this.eruptJdbc(model).value());

    @Override
    public Object findDataById(EruptModel eruptModel, Object id) {
        return table.findById(eruptModel, id);
    }

    @Override
    public Page queryList(EruptModel eruptModel, Page page, EruptQuery eruptQuery) {
        return table.query(eruptModel, page, eruptQuery);
    }

    @Override
    public Collection<Map<String, Object>> queryColumn(EruptModel eruptModel, List<Column> columns, EruptQuery eruptQuery) {
        return table.queryColumn(eruptModel, columns, eruptQuery);
    }

    @Override
    public void addData(EruptModel eruptModel, Object object) {
        table.insert(eruptModel, object);
    }

    @Override
    public void editData(EruptModel eruptModel, Object object) {
        table.update(eruptModel, object);
    }

    @Override
    public void deleteData(EruptModel eruptModel, Object object) {
        table.delete(eruptModel, object);
    }

    private EruptJdbc eruptJdbc(EruptModel eruptModel) {
        EruptJdbc eruptJdbc = eruptModel.getClazz().getAnnotation(EruptJdbc.class);
        if (null == eruptJdbc) {
            throw new EruptWebApiRuntimeException("@EruptJdbc annotation is missing on " + eruptModel.getEruptName());
        }
        return eruptJdbc;
    }

    private NamedParameterJdbcTemplate template(EruptJdbc eruptJdbc) {
        return templates.computeIfAbsent(eruptJdbc.datasource(), name -> new NamedParameterJdbcTemplate(
                name.isEmpty() ? applicationContext.getBean(DataSource.class)
                        : applicationContext.getBean(name, DataSource.class)));
    }

}
