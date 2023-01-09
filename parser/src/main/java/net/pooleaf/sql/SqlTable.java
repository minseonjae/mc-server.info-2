package net.pooleaf.sql;

import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
public class SqlTable {
	
	private final AbstractSqlManager sqlManager;
	
	private final String name, columnString;
	
	private final List<SqlColumn> columns;
	
	public SqlTable(AbstractSqlManager sqlManager, String name, String column) {
		this.sqlManager = sqlManager;
		
		this.name = name;

		columns = new ArrayList<>();
		
		String[] cs = column.split(",");
		for(int i = 0; i < cs.length; i++) {
			String c = cs[i].trim();
			
			if(c.toLowerCase().startsWith("primary key")) {
				String keyStr = c.substring("primary key(".length(), c.length() - 1);
				
				for(String key : keyStr.split(",")) {
					SqlColumn cm = getColumn(key.trim());
					if(cm == null) continue;
					
					cm.setPrimaryKey(true);
				}
				
				break;
			}
			
			String[] ccs = c.split(" ");
			
			String cName = ccs[0];
			String cType = ccs[1];
			boolean notNull = c.toLowerCase().contains("not null");
			boolean autoIncrement = c.toLowerCase().contains("auto_increment") || c.toLowerCase().contains("autoincrement");
			if (autoIncrement) {
				column = column.replace(c, c.replaceAll("(?i)autoincrement","auto_increment").replaceAll("(?i) int ", " integer "));
			}
			boolean primaryKey = c.toLowerCase().contains("primary key");
			
			SqlColumn sc = new SqlColumn(cName, cType, notNull, autoIncrement, primaryKey);
			columns.add(sc);
		}

		this.columnString = column;
	}
	
	public SqlColumn getColumn(String name) {
		for(SqlColumn ac : columns) {
			if(ac.getName().equalsIgnoreCase(name)) return ac;
		}
		
		return null;
	}
	
	public SqlTable createTable() {
		sqlManager.createTable(name, columnString);
		return this;
	}
	
	public void deleteTable() {
		sqlManager.deleteTable(name);
	}
	
	public void truncateTable() {
		sqlManager.truncateTable(name);
	}

	@SneakyThrows(SQLException.class)
	public void insert(Object...values) {
		replaceValues(values);

		@Cleanup PreparedStatement state = sqlManager.getPreparedStatement("insert into " + name + " values (" + getQuestionMark(values) + ")");
		int i = 0;
		for (Object value : values) {
			if (value == null || (value instanceof String && value.toString().startsWith("$$"))) continue;

			state.setObject(i + 1, value);
			i++;
		}

		state.executeUpdate();
	}

	@SneakyThrows(SQLException.class)
	public void insertIgnore(Object...values) {
		replaceValues(values);

		@Cleanup PreparedStatement state = sqlManager.getPreparedStatement("insert ignore into " + name + " values (" + getQuestionMark(values) + ")");
		int i = 0;
		for (Object value : values) {
			if (value == null || (value instanceof String && value.toString().startsWith("$$"))) continue;

			state.setObject(i + 1, value);
			i++;
		}

		state.executeUpdate();
	}

	@SneakyThrows(SQLException.class)
	public void insertDuplicate(Object...values) {
		replaceValues(values);

		Map<String, Object> ds = new LinkedHashMap<>();
		for (int i = 0; i < columns.size(); i++) {
			SqlColumn c = columns.get(i);
			if(c.isPrimaryKey() || c.isAutoIncrement()) continue;

			Object value = values[i];

			String q = "?";
			if (value == null) {
				q = "null";
			} else if (value instanceof String && value.toString().startsWith("$$")) {
				q = value.toString().substring(2);
			}

			ds.put(c.getName() + "=" + q, value == null || value.toString().startsWith("$$") ? null : value);
		}

		@Cleanup PreparedStatement state = sqlManager.getPreparedStatement("insert into " + name + " values (" + getQuestionMark(values) + ")"
				+ " on duplicate key update " + String.join(",", ds.keySet()));

		int i = 0;
		for (Object value : values) {
			if (value == null || (value instanceof String && value.toString().startsWith("$$"))) continue;

			state.setObject(i + 1, value);
			i++;
		}

		for (Object value : ds.values()) {
			if (value == null) continue;

			state.setObject(i + 1, value);
			i++;
		}

		state.executeUpdate();
	}
	
	public void update(String set) {
		sqlManager.update("update " + name + " set " + set);
	}

	public void update(String set, String sql) {
		sqlManager.update("update " + name + " set " + set + " " + sql);
	}

	@SneakyThrows(SQLException.class)
	public void update(String set, String sql, Object... params) {
		@Cleanup PreparedStatement state = sqlManager.getPreparedStatement("update " + name + " set " + set + " " + sql);
		replaceValues(params);
		for (int i = 0; i < params.length; i++) {
			state.setObject(i + 1, params[i]);
		}

		state.executeUpdate();
	}
	
	public void delete() {
		sqlManager.update("delete from " + name);
	}

	public void delete(String sql) {
		sqlManager.update("delete from " + name + " " + sql);
	}

	@SneakyThrows(SQLException.class)
	public void delete(String sql, Object... params) {
		@Cleanup PreparedStatement state = sqlManager.getPreparedStatement("delete from " + name + " " + sql);
		replaceValues(params);
		for (int i = 0; i < params.length; i++) {
			state.setObject(i + 1, params[i]);
		}

		state.executeUpdate();
	}
	
	public PreparedStatement select(String selected) {
		return sqlManager.getPreparedStatement("select " + selected + " from " + name);
	}

	public PreparedStatement select(String selected, String sql) {
		return sqlManager.getPreparedStatement("select " + selected + " from " + name + " " + sql);
	}

	@SneakyThrows(SQLException.class)
	public PreparedStatement select(String selected, String sql, Object... params) {
		PreparedStatement state = sqlManager.getPreparedStatement("select " + selected + " from " + name + " " + sql);
		replaceValues(params);
		for (int i = 0; i < params.length; i++) {
			state.setObject(i + 1, params[i]);
		}

		return state;
	}


	private String getQuestionMark(Object... values) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			Object value = values[i];

			String q = "?";
			if (value == null) {
				q = "null";
			} else if (value instanceof String && value.toString().startsWith("$$")) {
				q = value.toString().substring(2);
			}

			sb.append(sb.length() < 1 ? q : ", " + q);
		}

		return sb.toString();
	}

	private void replaceValues(Object... values) {
		for (int i = 0; i < values.length; i++) {
			if (values[i] == null) continue;
			else if (values[i] instanceof Boolean) {
				values[i] = (boolean) values[i] ? 1 : 0;
			}
		}
	}
	
}