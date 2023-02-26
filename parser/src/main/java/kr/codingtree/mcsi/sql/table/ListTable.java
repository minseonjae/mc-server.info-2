package kr.codingtree.mcsi.sql.table;

import kr.codingtree.mcsi.sql.SqlManager;
import net.pooleaf.sql.AbstractSqlTable;

import java.util.HashSet;

public class ListTable extends AbstractSqlTable {

    public ListTable(SqlManager manager) {
        super(manager, "server_list",
                "id int primary key not null auto_increment",
                "address varchar(64)",
                "port int",
                "name varchar(16)",
                "ban boolean");
    }

}
