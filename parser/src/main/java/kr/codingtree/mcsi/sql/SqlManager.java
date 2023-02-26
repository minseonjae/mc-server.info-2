package kr.codingtree.mcsi.sql;

import kr.codingtree.mcsi.sql.table.DataTable;
import kr.codingtree.mcsi.sql.table.ListTable;
import kr.codingtree.mcsi.sql.table.MotdTable;
import lombok.Getter;
import net.pooleaf.sql.AbstractSqlManager;

public class SqlManager extends AbstractSqlManager {

    @Getter
    private ListTable listTable;

    @Getter
    private DataTable dataTable;

    @Getter
    private MotdTable motdTable;


    @Override
    public void onConnected() {
        listTable = new ListTable(this);
        dataTable = new DataTable(this);
        motdTable = new MotdTable(this);
    }
}
