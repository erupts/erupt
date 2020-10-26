package xyz.erupt.bi.constant;

import lombok.Getter;

@Getter
public enum DBTypeEnum {
    Mysql(DBTypeEnum.GENERAL_LIMIT),
    PostgreSQL(DBTypeEnum.GENERAL_LIMIT),
    Oracle("SELECT t.* FROM (             \n" +
            "SELECT temp.*,ROWNUM RN             \n" +
            "FROM(@sql) temp                     \n" +
            "WHERE ROWNUM < @skip + @size + 1) t \n" +
            "WHERE RN > @skip"),
    //    SQLServer(null),
    Other(DBTypeEnum.GENERAL_LIMIT);

    public static final String $SQL = "@sql";
    public static final String $SIZE = "@size";
    public static final String $SKIP = "@skip";

    public static final String GENERAL_LIMIT = "select * from (" + $SQL + ") t limit " + $SIZE + " offset " + $SKIP;

    private final String limitSql;

    DBTypeEnum(String limitSql) {
        this.limitSql = limitSql;
    }
}
