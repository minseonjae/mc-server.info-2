package net.pooleaf.sqllib.dslcontext;

import lombok.SneakyThrows;
import net.pooleaf.core.modules.sqllib.common.AbstractSqlManager;
import net.pooleaf.core.modules.sqllib.common.CachedResult;
import net.pooleaf.core.modules.sqllib.common.CachedResultRow;
import net.pooleaf.core.modules.sqllib.common.SqlTable;
import net.pooleaf.core.modules.support.common.util.ReflectionUtil;
import net.pooleaf.core.modules.support.common.util.StringUtil;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SelectContext extends DslContext<SelectContext> {

    public SelectContext(AbstractSqlManager sqlManager, SqlTable sqlTable) {
        super(sqlManager, sqlTable);

        sqls.put("FROM", sqlTable.getName());
    }


    @Override
    public String buildSql() {
        StringBuilder builder = new StringBuilder();

        builder.append(getSql("MAIN"))
                .append(getSql("FROM"))
                .append(getSql("WHERE"))
                .append(getSql("GROUP BY"))
                .append(getSql("HAVING"))
                .append(getSql("ORDER BY"))
                .append(getSql("LIMIT"))
                .append(getSql("SQL"));

        return builder.toString();
    }


    public SelectContext select(String columns) {
        sqls.put("MAIN", "SELECT " + columns);
        return this;
    }

    public SelectContext where(String conditions) {
        sqls.put("WHERE", conditions);
        return this;
    }

    public SelectContext groupBy(String columns) {
        sqls.put("GROUP BY", columns);
        return this;
    }

    public SelectContext having(String conditions) {
        sqls.put("HAVING", conditions);
        return this;
    }

    public SelectContext orderBy(String columns) {
        sqls.put("ORDER BY", columns);
        return this;
    }

    public SelectContext orderBy(String columns, boolean asc) {
        sqls.put("ORDER BY", columns + " " + (asc ? "ASC" : "DESC"));
        return this;
    }

    public SelectContext limit(int count) {
        sqls.put("LIMIT", count + "");
        return this;
    }

    public SelectContext limit(int offset, int count) {
        sqls.put("LIMIT", offset + ", " + count);
        return this;
    }

    public SelectContext parameters(Object... parameters) {
        this.values = parameters;
        return this;
    }

    /**
     * SQL문을 실행하여 결과값을 반환합니다.
     * @return SQL문 실행 결과값
     */
    public CachedResult execute() {
        String sql = buildSql();

        if (values == null) {
            return sqlManager.getResult(sql);
        } else {
            return sqlManager.getResult(sql, values);
        }
    }

    /**
     * SQL문을 실행하여 결과값을 해당 클래스 객체로 반환합니다.
     * @return SQL문 실행 결과 객체
     */
    @SneakyThrows
    public <T> List<T> executeList(Class<T> objectClass) {
        List<Object> resultObjects = new ArrayList<>();

        CachedResult result = execute();
        for (CachedResultRow row : result.getRows()) {
            Object object = objectClass.newInstance();

            for (String key : row.getDatas().keySet()) {
                String targetFieldName = StringUtil.convertSnakeCaseToLowerCamelCase(key);
                Field targetField = ReflectionUtil.getFieldAll(objectClass, targetFieldName);
                if (targetField != null) {
                    Object value = replaceValue(targetField, row.get(key));

                    targetField.setAccessible(true);
                    targetField.set(object, value);
                }
            }
        }

        return (List<T>) resultObjects;
    }

    /**
     * 불러온 값으로 해당 클래스의 객체를 생성하여 반환합니다.
     */
    public <T> T execute(Class<T> objectClass) {
        List<T> objects = executeList(objectClass);

        if (objects.isEmpty()) {
            return null;
        }

        return (T) objects.get(0);
    }

    /**
     * 불러온 값들을 객체의 변수들에 넣어줍니다.
     */
    @SneakyThrows
    public <T> T execute(T object) {
        CachedResult result = execute();
        for (CachedResultRow row : result.getRows()) {
            for (String key : row.getDatas().keySet()) {
                String targetFieldName = StringUtil.convertSnakeCaseToLowerCamelCase(key);
                Field targetField = ReflectionUtil.getFieldAll(object.getClass(), targetFieldName);
                if (targetField != null) {
                    Object value = replaceValue(targetField, row.get(key));

                    targetField.setAccessible(true);
                    targetField.set(object, value);
                }
            }
        }

        return object;
    }

    /**
     * 값을 해당 필드에 맞는 타입으로 변환해줍니다.
     */
    private Object replaceValue(Field targetField, Object value) {
        // Timestamp -> LocalDateTime 변환
        if (value instanceof Timestamp) {
            if (targetField.getType().isAssignableFrom(LocalDateTime.class)) {
                value = ((Timestamp) value).toLocalDateTime();
            } else if (targetField.getType().isAssignableFrom(LocalDate.class)) {
                value = ((Timestamp) value).toLocalDateTime().toLocalDate();
            } else if (targetField.getType().isAssignableFrom(LocalTime.class)) {
                value = ((Timestamp) value).toLocalDateTime().toLocalTime();
            }
        }

        // UUID 변환
        if (targetField.getType().isAssignableFrom(UUID.class)) {
            value = UUID.fromString((String) value);
        }

        return value;
    }

}