package net.pooleaf.sqllib;

import lombok.Data;
import net.pooleaf.core.modules.sqllib.common.configs.SqlType;
import net.pooleaf.core.modules.sqllib.common.dslcontext.DeleteContext;
import net.pooleaf.core.modules.sqllib.common.dslcontext.InsertContext;
import net.pooleaf.core.modules.sqllib.common.dslcontext.SelectContext;
import net.pooleaf.core.modules.sqllib.common.dslcontext.UpdateContext;

import java.util.ArrayList;
import java.util.List;

@Data
public class SqlTable {

    private AbstractSqlManager sqlManager;

    private String name; // 테이블명

    private String columnString; // 테이블 생성 시 Column 텍스트
    private List<SqlColumn> columns = new ArrayList<>(); // 테이블 Column 목록


    public SqlTable(AbstractSqlManager sqlManager, String name, String... columnStrings) {
        this.sqlManager = sqlManager;

        this.name = name;

        this.columnString = String.join(", ", columnStrings);
        for (String columnString : columnStrings) {
            // PK 여러개일 경우
            // 예) PRIMARY KEY (column1, column2)
            if (columnString.toLowerCase().startsWith("primary key")) {

                continue;
            }

            // 일반 Column일 경우
            columnString = columnString.replaceAll("(?i) INT ", " INTEGER "); // SQL Type마다 문법이 다르므로 INT를 INTEGER로 통일

            String columnName = columnString.substring(0, columnString.indexOf(" "));
            String columnType = columnString.split(" ")[1];

            boolean notNull = columnString.toLowerCase().contains("not null");
            boolean autoIncrement = columnString.toLowerCase().contains("auto_increment") || columnString.toLowerCase().contains("autoincrement");
            // SQL Type마다 Auto Increment 문법이 다르므로 변환
            if (autoIncrement) {
                if (sqlManager.getConfig().getSqlType() == SqlType.SQLITE) {
                    columnString = columnString.replaceAll("(?i)AUTO_INCREMENT", "AUTOINCREMENT");
                } else {
                    columnString = columnString.replaceAll("(?i)AUTOINCREMENT", "AUTO_INCREMENT");
                }
            }
            boolean primaryKey = columnString.toLowerCase().contains("primary key");

            SqlColumn sqlColumn = new SqlColumn();
            sqlColumn.setName(columnName);
            sqlColumn.setType(columnType);
            sqlColumn.setNotNull(notNull);
            sqlColumn.setAutoIncrement(autoIncrement);
            sqlColumn.setPrimaryKey(primaryKey);

            columns.add(sqlColumn);
        }
    }

    public SqlColumn getColumn(String name) {
        for (SqlColumn column : columns) {
            if (column.getName().equals(name)) {
                return column;
            }
        }

        return null;
    }

    /**
     * 테이블을 생성합니다.
     */
    public SqlTable create() {
        sqlManager.createTable(name, columnString);
        return this;
    }

    /**
     * 테이블을 삭제합니다.
     */
    public void drop() {
        sqlManager.dropTable(name);
    }

    /**
     * 테이블을 초기화시킵니다 (복구 불가)
     */
    public void truncate() {
        sqlManager.truncateTable(name);
    }

    /**
     * 테이블의 데이터 수를 반환합니다.
     * @return
     */
    public int count() {
        return select("COUNT(*)")
                .execute()
                .getRow(0).getInt("COUNT(*)");
    }

    /**
     * SelectContext를 작성합니다.
     * @param columns , 로 이어진 컬럼명
     */
    public SelectContext select(String columns) {
        SelectContext context = new SelectContext(sqlManager, this);
        return context.select(columns);
    }

    /**
     * 모든 Column을 조회하는 SelectContext를 작성합니다.
     */
    public SelectContext select() {
        SelectContext context = new SelectContext(sqlManager, this);
        return context.select("*");
    }

    /**
     * DeleteContext를 작성합니다.
     */
    public DeleteContext delete() {
        DeleteContext context = new DeleteContext(sqlManager, this);
        return context;
    }

    /**
     * UpdateContext를 작성합니다.
     */
    public UpdateContext update() {
        UpdateContext context = new UpdateContext(sqlManager, this);
        return context;
    }

    /**
     * InsertContext를 작성합니다.
     */
    public InsertContext insertInto() {
        InsertContext context = new InsertContext(sqlManager, this);
        return context.insertInto();
    }

    /**
     * InsertContext를 작성합니다.
     * @param columns , 로 이어진 컬럼명
     */
    public InsertContext insertInto(String columns) {
        InsertContext context = new InsertContext(sqlManager, this);
        return context.insertInto(columns);
    }

    /**
     * InsertContext를 작성합니다.
     */
    public InsertContext insertIgnoreInto() {
        InsertContext context = new InsertContext(sqlManager, this);
        return context.insertIgnoreInto();
    }

    /**
     * InsertContext를 작성합니다.
     * @param columns , 로 이어진 컬럼명
     */
    public InsertContext insertIgnoreInto(String columns) {
        InsertContext context = new InsertContext(sqlManager, this);
        return context.insertIgnoreInto(columns);
    }

}
