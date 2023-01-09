package net.pooleaf.sqllib.dslcontext;

import kr.codingtree.mcsi.MCServerInfo;
import lombok.Cleanup;
import lombok.SneakyThrows;
import net.pooleaf.sqllib.AbstractSqlManager;
import net.pooleaf.sqllib.SqlTable;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DeleteContext extends DslContext<DeleteContext> {

    private List<Object> values;

    private List<List<Object>> batches = new ArrayList<>(); // Batch로 Insert문 여러개 사용할 때 값들을 저장해둘 List


    public DeleteContext(AbstractSqlManager sqlManager, SqlTable sqlTable) {
        super(sqlManager, sqlTable);

        sqls.put("MAIN", "DELETE FROM " + sqlTable.getName());
    }


    @Override
    public String buildSql() {
        StringBuilder builder = new StringBuilder();

        builder.append(getSql("MAIN"))
                .append(getSql("WHERE"))
                .append(getSql("SQL"));

        return builder.toString();
    }


    public DeleteContext where(String conditions) {
        sqls.put("WHERE", conditions);
        return this;
    }

    public DeleteContext parameters(Object... parameters) {
        addBatch();

        this.values = Arrays.asList(parameters);
        return this;
    }

    /**
     * Batch에 값을 저장하고 현재 값을 초기화합니다.
     * Batch에 저장된 값은 execute시 함께 저장됩니다.
     * @return
     */
    @SneakyThrows
    private DeleteContext addBatch() {
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
     * SQL문을 실행하고 삭제 성공한 수를 반환합니다.
     * @return 삭제 성공한 수
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
        mcsi.getConsole().debugLog("[SQLib] Delete SQL: " + sql);

        @Cleanup PreparedStatement statement = sqlManager.preparedStatement(sql);
        for (List<Object> batchValues : batches) {
            mcsi.getConsole().debugLog("[SQLib] Delete Parameters: [" + batchValues.stream().map(value -> (String) value).collect(Collectors.joining(", ")) + "]");

            // 값 처리
            int i = 1;
            for (Object batchValue : batchValues) {
                statement.setObject(i, sqlManager.convertValue(batchValue));
                i++;
            }

            statement.addBatch();
            statement.clearParameters();
        }

        // 실행 후 결과값 출력
        int[] result = statement.executeBatch();
        mcsi.getConsole().debugLog("[SQLib] Insert Result: " + Arrays.stream(result)
                .mapToObj(r -> String.valueOf(r))
                .collect(Collectors.joining(", ")));

        mcsi.getConsole().debugLog("[SQLib] Insert Executed in " + (System.currentTimeMillis() - startTime) + " ms");

        return result;
    }

}
