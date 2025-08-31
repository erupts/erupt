package xyz.erupt.jpa.support;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * @author YuePeng
 * date 2025/8/30 21:56
 */
public class LongTextType implements UserType<String> {
    @Override
    public int getSqlType() {
        // Hibernate 6 会根据方言把 CLOB 转成各库自己的“大文本”
        return Types.LONGVARCHAR;
    }

    @Override
    public Class<String> returnedClass() {
        return String.class;
    }

    @Override
    public String nullSafeGet(ResultSet rs, int position,
                              SharedSessionContractImplementor session, Object owner)
            throws SQLException {
        return rs.getString(position);   // 统一按字符串读
    }

    @Override
    public void nullSafeSet(PreparedStatement st, String value, int index,
                            SharedSessionContractImplementor session)
            throws SQLException {
        if (value == null) {
            st.setNull(index, java.sql.Types.CLOB);
        } else {
            st.setString(index, value);  // 统一按字符串写
        }
    }

    @Override
    public boolean equals(String x, String y) {
        return java.util.Objects.equals(x, y);
    }

    @Override
    public int hashCode(String x) {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public String deepCopy(String value) {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(String value) {
        return value;
    }

    @Override
    public String assemble(Serializable cached, Object owner) {
        return (String) cached;
    }

    @Override
    public String replace(String original, String target, Object owner) {
        return original;
    }

}
