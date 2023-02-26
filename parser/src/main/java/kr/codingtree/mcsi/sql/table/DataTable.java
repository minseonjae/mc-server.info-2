package kr.codingtree.mcsi.sql.table;

import kr.codingtree.mcsi.sql.SqlManager;
import net.pooleaf.sql.AbstractSqlTable;

public class DataTable extends AbstractSqlTable {

    public DataTable(SqlManager manager) {
        super(manager, "server_data",
                "id int",
                "ping int",
                "protocol int",
                "version varchar(64)",
                "max_players int",
                "online_players int",
                "motd varchar(255)",
                "srv boolean",
                "time datetime");
    }

}
