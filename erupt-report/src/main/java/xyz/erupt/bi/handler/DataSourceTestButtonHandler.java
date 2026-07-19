package xyz.erupt.bi.handler;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.EruptButtonHandler;
import xyz.erupt.bi.model.BiDataSource;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.jpa.dao.EruptDao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.util.Optional;

/**
 * @author YuePeng
 * date 2026-07-19
 */
@Service
public class DataSourceTestButtonHandler implements EruptButtonHandler<BiDataSource> {

    @Resource
    private EruptDao eruptDao;

    @Override
    public String click(BiDataSource biDataSource, String[] params) {
        requireField(biDataSource.getDriver(), "Driver");
        requireField(biDataSource.getUrl(), "Connection String");
        // In the edit form the frontend sends a placeholder instead of the real password
        if (null != biDataSource.getId() && (null == biDataSource.getPassword() || biDataSource.getPassword().isBlank()
                || EruptConst.PASSWORD_PLACEHOLDER.equals(biDataSource.getPassword()))) {
            Optional.ofNullable(eruptDao.lambdaQuery(BiDataSource.class).eq(BiDataSource::getId, biDataSource.getId()).one())
                    .ifPresent(it -> biDataSource.setPassword(it.getPassword()));
        }
        try {
            Class.forName(biDataSource.getDriver());
        } catch (ClassNotFoundException e) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate("JDBC Driver class not found: ") + biDataSource.getDriver());
        }
        try (Connection connection = DriverManager.getConnection(
                biDataSource.getUrl(), biDataSource.getUserName(), biDataSource.getPassword())) {
            DatabaseMetaData metaData = connection.getMetaData();
            return "alert(" + GsonFactory.getGson().toJson(
                    metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion()) + ")";
        } catch (Exception e) {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
    }

    private void requireField(String value, String title) {
        if (null == value || value.isBlank()) {
            throw new EruptWebApiRuntimeException(I18nTranslate.$translate(title) + " " + I18nTranslate.$translate("erupt.notnull"));
        }
    }

}
