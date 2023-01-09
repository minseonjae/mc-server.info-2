package net.pooleaf.sqllib.dslcontext;

import lombok.Cleanup;
import lombok.SneakyThrows;
import net.pooleaf.core.modules.sqllib.common.AbstractSqlManager;
import net.pooleaf.core.modules.sqllib.common.SqlColumn;
import net.pooleaf.core.modules.sqllib.common.SqlTable;
import net.pooleaf.core.modules.sqllib.common.configs.SqlType;
import net.pooleaf.core.modules.support.common.debugger.Debugger;
import net.pooleaf.core.modules.support.common.util.ReflectionUtil;
import net.pooleaf.core.modules.support.common.util.StringUtil;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class InsertContext extends DslContext<InsertContext> implements Cloneable {

    private List<Object> values;

    private List<List<Object>> batches = new ArrayList<>(); // Batch로 Insert문 여러개 사용할 때 값들을 저장해둘 List

    private List<SqlColumn> insertColumns = new ArrayList<>(); // INSERT INTO에 적어준 Column


    public InsertContext(AbstractSqlManager sqlManager, SqlTable sqlTable) {
        super(sqlManager, sqlTable);
    }


    @Override
    public String buildSql() {
        // SQLite ON DUPLICATE KEY UPDATE 문법 처리
        if (sqlManager.getConfig().getSqlType() == SqlType.SQLITE
                && sqls.containsKey("ON DUPLICATE KEY UPDATE")) {
            sqls.put("MAIN", sqls.get("MAIN").replace("INSERT INTO", "INSERT OR REPLACE INTO"));
            sqls.remove("ON DUPLICATE KEY UPDATE");
        }


        StringBuilder builder = new StringBuilder();

        builder.append(getSql("MAIN"))
                .append(getSql("VALUES"))
                .append(getSql("ON DUPLICATE KEY UPDATE"))
                .append(getSql("SQL"));

        return builder.toString();
    }


    /**
     * SqlTable의 모든 Column을 INSERT INTO 뒤 Column으로 사용하고, Column 이름을 ,로 이어 반환합니다.
     * @return SqlTable의 모든 Column 이름을 ,로 이은 문자
     */
    private String insertColumns() {
        insertColumns = sqlTable.getColumns();
        String columns = insertColumns.stream()
                .map(SqlColumn::getName)
                .collect(Collectors.joining(", "));

        return columns;
    }

    /**
     * 입력한 Columns를 ,로 나눠 INSERT INTO 뒤 Column으로 사용합니다.
     */
    private void insertColumns(String columns) {
        for (String column : columns.split(", ")) {
            column = column.trim();
            insertColumns.add(sqlTable.getColumn(column));
        }
    }

    public InsertContext insertInto() {
        String columns = insertColumns();
        sqls.put("MAIN", "INSERT INTO " + sqlTable.getName() + " (" + columns + ")");
        return this;
    }

    public InsertContext insertInto(String columns) {
        insertColumns(columns);
        sqls.put("MAIN", "INSERT INTO " + sqlTable.getName() + " (" + columns + ")");
        return this;
    }

    public InsertContext insertIgnoreInto() {
        String columns = insertColumns();
        sqls.put("MAIN", "INSERT IGNORE INTO " + sqlTable.getName() + " (" + columns + ")");
        return this;
    }

    public InsertContext insertIgnoreInto(String columns) {
        insertColumns(columns);
        sqls.put("MAIN", "INSERT IGNORE INTO " + sqlTable.getName() + " (" + columns + ")");
        return this;
    }

    public InsertContext values(Object... values) {
        addBatch();

        this.values = new ArrayList<>();

        StringBuilder questionMarkBuilder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (questionMarkBuilder.length() > 0) {
                questionMarkBuilder.append(", ");
            }
            questionMarkBuilder.append("?");
        }

        sqls.put("VALUES", "(" + questionMarkBuilder.toString() + ")");
        Arrays.stream(values).forEach(value -> this.values.add(sqlManager.convertValue(value)));

        return this;
    }

    @SneakyThrows
    public InsertContext valuesByObject(Object valueObject) {
        addBatch();

        this.values = new ArrayList<>();

        StringBuilder questionMarkBuilder = new StringBuilder();
        for (SqlColumn insertColumn : insertColumns) {
            if (questionMarkBuilder.length() > 0) {
                questionMarkBuilder.append(", ");
            }
            questionMarkBuilder.append("?");

            // 객체에서 값 꺼내오기
            Field field = ReflectionUtil.getFieldAll(valueObject.getClass(), StringUtil.convertSnakeCaseToLowerCamelCase(insertColumn.getName()));
            field.setAccessible(true);
            Object value = field.get(valueObject);
            values.add(sqlManager.convertValue(value));
        }

        sqls.put("VALUES", "(" + questionMarkBuilder.toString() + ")");

        return this;
    }

    public InsertContext onDuplicateKeyUpdate() {
        StringBuilder sqlBuilder = new StringBuilder();
        for (int i = 0; i < insertColumns.size(); i++) {
            SqlColumn insertColumn = insertColumns.get(i);

            // PK면 생략
            if (insertColumn.isPrimaryKey()) {
                continue;
            }

            if (sqlBuilder.length() > 0) {
                sqlBuilder.append(", ");
            }
            sqlBuilder.append(insertColumn.getName() + " = ?");

            values.add(values.get(i));
        }

        sqls.put("ON DUPLICATE KEY UPDATE", sqlBuilder.toString());
        return this;
    }

    /**
     * Batch에 값을 저장하고 현재 값을 초기화합니다.
     * Batch에 저장된 값은 execute시 함께 저장됩니다.
     * @return
     */
    @SneakyThrows
    private InsertContext addBatch() {
        // 값 설정 안돼있으면 실행 안함
        if (values == null || values.isEmpty()) {
            return this;
        }

        batches.add(values);

        // 값 초기화
        values = null;

        return this;
    }

    /**
     * SQL문을 실행합니다.
     * addBatch로 추가한 값들을 순차적으로 저장합니다.
     * @return 결과값 배열
     */
    @SneakyThrows
    public int[] execute() {
        long startTime = System.currentTimeMillis();

        // 설정된 값이 있으면 addBatch
        if (values != null && !values.isEmpty()) {
            addBatch();
        }

        // Batch 비어있으면 중단
        if (batches.isEmpty()) {
            return null;
        }

        String sql = buildSql();
        Debugger.log("[SQLib] Insert SQL: " + sql);

        @Cleanup PreparedStatement statement = sqlManager.preparedStatement(sql);
        for (List<Object> batchValues : batches) {
            Debugger.log("[SQLib] Insert Parameters: [" + batchValues.stream().map(value -> (String) value).collect(Collectors.joining(", ")) + "]");

            // 값 처리
            int i = 1;
            for (Object batchValue : batchValues) {
                statement.setObject(i, sqlManager.convertValue(batchValue));
                i++;
            }

            // ON DUPLICATE KEY UPDATE 시 추가 값 처리
            if (sql.contains("ON DUPLICATE KEY UPDATE")) {
                int start = i;
                for (Object batchValue : batchValues) {
                    // 해당 Column이 PK라면 건너뛰기
                    if (insertColumns.get(i - start).isPrimaryKey()) {
                        continue;
                    }

                    statement.setObject(i, sqlManager.convertValue(batchValue));
                    i++;
                }
            }

            statement.addBatch();
            statement.clearParameters();
        }

        // 실행 후 결과값 출력
        int[] result = statement.executeBatch();
        Debugger.log("[SQLib] Insert Result: " + Arrays.stream(result)
                .mapToObj(r -> String.valueOf(r))
                .collect(Collectors.joining(", ")));

        Debugger.log("[SQLib] Insert Executed in " + (System.currentTimeMillis() - startTime) + " ms");

        return result;
    }

    /**
     * SQL문을 실행합니다.
     * 여러 값 Insert를 지원하지 않습니다.
     * @return 생성된 키
     */
    @SneakyThrows
    public Long executeWithGeneratedKey() {
        long startTime = System.currentTimeMillis();

        if (values != null && values.size() > 0) {
            addBatch();
        }

        // Batch 비어있으면 중단
        if (batches.isEmpty()) {
            return null;
        }

        String sql = buildSql();
        Debugger.log("[SQLib] Insert SQL: " + sql);

        List<Object> batchValues = batches.get(0);
        Debugger.log("[SQLib] Insert Parameters: [" + batchValues.stream().map(value -> value.toString()).collect(Collectors.joining(", ")) + "]");

        @Cleanup PreparedStatement statement = sqlManager.preparedStatement(sql);

        // 값 처리
        int i = 1;
        for (Object batchValue : batchValues) {
            statement.setObject(i, sqlManager.convertValue(batchValue));
            i++;
        }

        // ON DUPLICATE KEY UPDATE 시 추가 파라미터 값 처리
        if (sql.contains("ON DUPLICATE KEY UPDATE")) {
            int start = i;
            for (Object batchValue : batchValues) {
                // 해당 Column이 PK라면 건너뛰기
                if (insertColumns.get(i - start).isPrimaryKey()) {
                    continue;
                }

                statement.setObject(i, sqlManager.convertValue(batchValue));
                i++;
            }
        }

        // 실행 후 결과값 출력
        int result = statement.executeUpdate();
        Debugger.log("[SQLib] Insert Result: " + result);

        // 생성된 키 처리
        Long generatedKey = null;
        @Cleanup ResultSet generatedKeyResult = statement.getGeneratedKeys();
        while (generatedKeyResult.next()) {
            generatedKey = generatedKeyResult.getLong(1);
        }
        Debugger.log("[SQLib] Generated key: " + generatedKey);

        Debugger.log("[SQLib] Insert executed in " + (System.currentTimeMillis() - startTime) + " ms");

        return generatedKey;
    }

}
