package net.pooleaf.sqllib.dslcontext;

import kr.codingtree.mcsi.MCServerInfo;
import net.pooleaf.core.modules.sqllib.common.AbstractSqlManager;
import net.pooleaf.core.modules.sqllib.common.SqlTable;

import java.util.HashMap;
import java.util.Map;

public abstract class DslContext<T extends DslContext> {

    protected static MCServerInfo mcsi = MCServerInfo.getInstance();
    protected final AbstractSqlManager sqlManager;
    protected final SqlTable sqlTable;

    protected Map<String, String> sqls = new HashMap<>();
    protected Object[] values;


    public DslContext(AbstractSqlManager sqlManager, SqlTable sqlTable) {
        this.sqlManager = sqlManager;
        this.sqlTable = sqlTable;
    }


    /**
     * 완성된 SQL문 끝에 SQL문을 작성합니다.
     * @param sql SQL문
     */
    public T sql(String sql) {
        sqls.put("SQL", sql);
        return (T) this;
    }


    protected String getSql(String key) {
        String sql = "";

        if (sqls.containsKey(key)) {
            if (key.equals("MAIN")) {
                sql = sqls.get(key);
            } else {
                sql = " " + key + " " + sqls.get(key);
            }
        }

        return sql;
    }

    public abstract String buildSql();

}
