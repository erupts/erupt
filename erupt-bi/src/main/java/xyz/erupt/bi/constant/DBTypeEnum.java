package xyz.erupt.bi.constant;

import lombok.Getter;

@Getter
public enum DBTypeEnum {
    MySQL(DBTypeEnum.GENERAL_LIMIT),
    PostgreSQL(DBTypeEnum.GENERAL_LIMIT),
    Oracle(DBTypeEnum.ORACLE_LIMIT),
    SQLServer(DBTypeEnum.SQL_SERVER_LIMIT),
    Other(DBTypeEnum.GENERAL_LIMIT);

    public static final String $SQL = "@sql";
    public static final String $SIZE = "@size";
    public static final String $SKIP = "@skip";

    public static final String GENERAL_LIMIT = "select * from (" + $SQL + ") t limit " + $SIZE + " offset " + $SKIP;

    private static final String ORACLE_LIMIT = "SELECT t.* FROM (  \n" +
            "SELECT ROWNUM RN,temp.*                               \n" +
            "FROM(" + $SQL + ") temp                               \n" +
            "WHERE ROWNUM < " + $SKIP + " + " + $SIZE + " + 1) t   \n" +
            "WHERE RN > " + $SKIP;

    //    支持2012及以上版本
    private static final String SQL_SERVER_LIMIT = "SELECT * from (" + $SQL + ") t\n" +
            "OFFSET " + $SKIP + " ROWS\n" +
            "FETCH NEXT " + $SIZE + " ROWS ONLY";

    private final String limitSql;

    DBTypeEnum(String limitSql) {
        this.limitSql = limitSql;
    }
}
