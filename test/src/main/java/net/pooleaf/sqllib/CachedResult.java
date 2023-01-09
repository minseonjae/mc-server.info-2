package net.pooleaf.sqllib;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;

import java.sql.ResultSet;
import java.util.*;

@Getter
@ToString
public class CachedResult {

    private List<CachedResultRow> rows = new ArrayList<>();


    @SneakyThrows
    public CachedResult(ResultSet resultSet) {
        while (resultSet.next()) {
            Map<String, Object> resultRowDatas = new HashMap<>();

            for (int column = 0; column < resultSet.getMetaData().getColumnCount(); column++) {
                resultRowDatas.put(resultSet.getMetaData().getColumnName(column + 1), resultSet.getObject(column + 1));
            }

            CachedResultRow resultRow = new CachedResultRow(resultRowDatas);
            rows.add(resultRow);
        }

        Collections.unmodifiableList(rows);
    }

    public CachedResultRow getRow(int index) {
        return rows.get(index);
    }

}
