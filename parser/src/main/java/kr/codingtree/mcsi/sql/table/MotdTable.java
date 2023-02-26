package kr.codingtree.mcsi.sql.table;

import kr.codingtree.mcsi.sql.SqlManager;
import net.pooleaf.sql.AbstractSqlTable;

public class MotdTable extends AbstractSqlTable {

    public MotdTable(SqlManager manager) {
        super(manager, "server_motd",
                "id int",
                "motd varchar(255)",
                "time datetime");
    }
}
