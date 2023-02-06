package xyz.erupt.bi.constant;

import org.apache.commons.lang3.StringUtils;

public enum DBTypeEnum {
    MySQL(DBTypeEnum.GENERAL_LIMIT),
    MariaDB(DBTypeEnum.GENERAL_LIMIT),
    PostgreSQL(DBTypeEnum.GENERAL_LIMIT),
    TiDB(DBTypeEnum.GENERAL_LIMIT),
    Oracle(DBTypeEnum.ORACLE_LIMIT),
    SQLServer2012(DBTypeEnum.SQL_SERVER_2012_LIMIT),
    达梦(DBTypeEnum.ORACLE_LIMIT),
    人大金仓(DBTypeEnum.GENERAL_LIMIT),
    Clickhouse(DBTypeEnum.GENERAL_LIMIT),
    Impala(DBTypeEnum.GENERAL_LIMIT) {
        @Override
        public String processDialect(String dialect, String sql, String sort, Integer index, Integer size) {
            if (null == sort) {
                dialect = dialect.replace($SORT, "order by null");
            } else {
                dialect = dialect.replace($SORT, "order by " + sort);
            }
            return super.processDialect(dialect, sql, sort, index, size);
        }
    },
    StarRocks(DBTypeEnum.GENERAL_LIMIT),
    Other(null);

    private static final String $SQL = "@sql";
    private static final String $SIZE = "@size";
    private static final String $SKIP = "@skip";
    private static final String $SORT = "@sort";

    public static final String GENERAL_LIMIT = "select * from (" + $SQL + ") _t " + $SORT + " limit " + $SIZE + " offset " + $SKIP;

    private static final String ORACLE_LIMIT = "SELECT t.* FROM ( " +
            "SELECT ROWNUM RN,temp.* FROM(" + $SQL + ") temp " +
            "WHERE ROWNUM < " + $SKIP + " + " + $SIZE + " + 1) t " +
            "WHERE RN > " + $SKIP + " " + $SORT;

    private static final String SQL_SERVER_2012_LIMIT = "SELECT * from (" + $SQL + ") t " + $SORT + " OFFSET " + $SKIP + " ROWS FETCH NEXT " + $SIZE + " ROWS ONLY";

    private final String dialect;

    DBTypeEnum(String sqlDialect) {
        this.dialect = sqlDialect;
    }

    public String processDialect(String sql, String sort, Integer index, Integer size) {
        return processDialect(this.dialect, sql, sort, index, size);
    }

    public String processDialect(String dialect, String sql, String sort, Integer index, Integer size) {
        String result = dialect.replace(DBTypeEnum.$SQL, sql).replace(DBTypeEnum.$SIZE, String.valueOf(size))
                .replace(DBTypeEnum.$SKIP, String.valueOf((index - 1) * size));
        if (StringUtils.isNotBlank(sort)) {
            return result.replace(DBTypeEnum.$SORT, String.format("order by %s", sort));
        } else {
            return result.replace(DBTypeEnum.$SORT, "");
        }
    }

}
