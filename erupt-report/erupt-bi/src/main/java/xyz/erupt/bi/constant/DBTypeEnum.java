package xyz.erupt.bi.constant;

import lombok.Getter;

@Getter
public enum DBTypeEnum {
    MySQL(DBTypeEnum.GENERAL_LIMIT),
    MariaDB(DBTypeEnum.GENERAL_LIMIT),
    PostgreSQL(DBTypeEnum.GENERAL_LIMIT),
    Clickhouse(DBTypeEnum.GENERAL_LIMIT),
    Impala(DBTypeEnum.GENERAL_LIMIT),
    Oracle(DBTypeEnum.ORACLE_LIMIT),
    SQLServer2012(DBTypeEnum.SQL_SERVER_2012_LIMIT),
    达梦(DBTypeEnum.ORACLE_LIMIT),
    人大金仓(DBTypeEnum.GENERAL_LIMIT),
    Other(null);

    public static final String OTHER = Other.name();

    public static final String $SQL = "@sql";
    public static final String $SIZE = "@size";
    public static final String $SKIP = "@skip";

    public static final String $SORT = "@sort";

    public static final String GENERAL_LIMIT = "select * from (" + $SQL + ") t order by " + $SORT + " limit " + $SIZE + " offset " + $SKIP;

    private static final String ORACLE_LIMIT = "SELECT t.* FROM (  \n" +
            "SELECT ROWNUM RN,temp.*                               \n" +
            "FROM(" + $SQL + ") temp                               \n" +
            "WHERE ROWNUM < " + $SKIP + " + " + $SIZE + " + 1) t   \n" +
            "WHERE RN > " + $SKIP +
            " order by " + $SORT;

    private static final String SQL_SERVER_2012_LIMIT = "SELECT * from (" + $SQL + ") t \n" +
            "order by " + $SORT +
            " OFFSET " + $SKIP + " ROWS FETCH NEXT " + $SIZE + " ROWS ONLY";

    private final String limitSql;

    DBTypeEnum(String limitSql) {
        this.limitSql = limitSql;
    }
}
