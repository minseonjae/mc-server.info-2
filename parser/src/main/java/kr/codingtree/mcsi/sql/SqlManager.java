package kr.codingtree.mcsi.sql;

import net.pooleaf.sql.AbstractSqlManager;
import net.pooleaf.sql.SqlTable;

public class SqlManager extends AbstractSqlManager {

    private SqlTable listTable, dataTable;

    @Override
    public void createTable() {
        listTable = new SqlTable(this, "server_list", "id int primary key not null auto_increment, address varchar(64), port int, name varchar(16), ban int").createTable();
        dataTable = new SqlTable(this, "server_data", "id int, ping int, protocol int, version varchar(64), max_players int, online_players int, motd varchar(255), srv int, time datetime").createTable();
    }
}
