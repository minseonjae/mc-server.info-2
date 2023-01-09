package net.pooleaf.sqllib;

public abstract class SqlDao {

    protected AbstractSqlManager sqlManager;


    public SqlDao(AbstractSqlManager sqlManager) {
        this.sqlManager = sqlManager;
        sqlManager.getDaos().add(this);
    }

    public void onConnected() {}

}
