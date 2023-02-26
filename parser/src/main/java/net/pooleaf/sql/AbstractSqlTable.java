package net.pooleaf.sql;

import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Getter
public class AbstractSqlTable {
	
	private final AbstractSqlManager sqlManager;
	
	private final String name;

	private final String[] columns;
	
	private final List<SqlColumn> columnList;
	
	public AbstractSqlTable(AbstractSqlManager sqlManager, String name, String... columns) {
		this.sqlManager = sqlManager;
		this.name = name;
		columnList = new ArrayList<>();

		for(int i = 0; i < columns.length; i++) {
			String column = columns[i].trim();
			
			if(column.toLowerCase().startsWith("primary key")) {
				String keyStr = column.substring("primary key(".length(), column.length() - 1);
				
				for(String key : keyStr.split(",")) {
					SqlColumn cm = getColumn(key.trim());
					if(cm == null) continue;
					
					cm.setPrimaryKey(true);
				}
				
				break;
			}
			
			String[] ccs = column.split(" ");
			
			String cName = ccs[0];
			String cType = ccs[1];
			boolean notNull = column.toLowerCase().contains("not null");
			boolean autoIncrement = column.toLowerCase().contains("auto_increment") || column.toLowerCase().contains("autoincrement");
			if (autoIncrement) {
				column = column.replace(column, column.replaceAll("(?i)autoincrement","auto_increment").replaceAll("(?i) int ", " integer "));
			}
			boolean primaryKey = column.toLowerCase().contains("primary key");
			
			SqlColumn sc = new SqlColumn(cName, cType, notNull, autoIncrement, primaryKey);
			this.columnList.add(sc);
		}

		this.columns = columns;

		createTable();
	}
	
	protected SqlColumn getColumn(String name) {
		for(SqlColumn ac : columnList) {
			if(ac.getName().equalsIgnoreCase(name)) return ac;
		}
		
		return null;
	}
	
	protected AbstractSqlTable createTable() {
		StringBuilder columnString = new StringBuilder();
		for (String column : columns) {
			if (column.length() > 0) {
				columnString.append(",");
			}
			columnString.append(column);
		}
		sqlManager.createTable(name, columnString.toString());
		return this;
	}
	
	protected void deleteTable() {
		sqlManager.deleteTable(name);
	}
	
	protected void truncateTable() {
		sqlManager.truncateTable(name);
	}

	@SneakyThrows(SQLException.class)
	protected void insert(Object...values) {
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
	protected void insertIgnore(Object...values) {
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
	protected void insertDuplicate(Object...values) {
		replaceValues(values);

		Map<String, Object> ds = new LinkedHashMap<>();
		for (int i = 0; i < columnList.size(); i++) {
			SqlColumn c = columnList.get(i);
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

		@Cleanup PreparedStatement state = sqlManager.getPreparedStatement("insert into " + name + " values (" + getQuestionMark(values) + ") on duplicate key update " + String.join(",", ds.keySet()));

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
	
	protected void update(String set) {
		sqlManager.update("update " + name + " set " + set);
	}

	protected void update(String set, String sql) {
		sqlManager.update("update " + name + " set " + set + " " + sql);
	}

	@SneakyThrows(SQLException.class)
	protected void update(String set, String sql, Object... params) {
		@Cleanup PreparedStatement state = sqlManager.getPreparedStatement("update " + name + " set " + set + " " + sql);
		replaceValues(params);
		for (int i = 0; i < params.length; i++) {
			state.setObject(i + 1, params[i]);
		}

		state.executeUpdate();
	}
	
	protected void delete() {
		sqlManager.update("delete from " + name);
	}

	protected void delete(String sql) {
		sqlManager.update("delete from " + name + " " + sql);
	}

	@SneakyThrows(SQLException.class)
	protected void delete(String sql, Object... params) {
		@Cleanup PreparedStatement state = sqlManager.getPreparedStatement("delete from " + name + " " + sql);
		replaceValues(params);
		for (int i = 0; i < params.length; i++) {
			state.setObject(i + 1, params[i]);
		}

		state.executeUpdate();
	}
	
	protected PreparedStatement select(String selected) {
		return sqlManager.getPreparedStatement("select " + selected + " from " + name);
	}

	protected PreparedStatement select(String selected, String sql) {
		return sqlManager.getPreparedStatement("select " + selected + " from " + name + " " + sql);
	}

	@SneakyThrows(SQLException.class)
	protected PreparedStatement select(String selected, String sql, Object... params) {
		PreparedStatement state = sqlManager.getPreparedStatement("select " + selected + " from " + name + " " + sql);
		replaceValues(params);
		for (int i = 0; i < params.length; i++) {
			state.setObject(i + 1, params[i]);
		}

		return state;
	}

	@SneakyThrows(SQLException.class)
	protected int count(String sql, Object... params) {
		@Cleanup PreparedStatement state = select("count(*)", sql);
		for (int i = 0; i < params.length; i++) {
			state.setObject(i + 1, params[i]);
		}
		@Cleanup ResultSet rs = state.executeQuery();
		return rs.next() ? rs.getInt("count(*)") : 0;
	}

	@SneakyThrows(SQLException.class)
	protected boolean exists(String sql, Object... params) {
		@Cleanup PreparedStatement state = select("*", sql);
		for (int i = 0; i < params.length; i++) {
			state.setObject(i + 1, params[i]);
		}
		@Cleanup ResultSet rs = state.executeQuery();
		return rs.next();
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
			} else if (values[i] instanceof Timestamp) {
				values[i] = ((Timestamp) values[i]).toLocalDateTime().toString();
			}
		}
	}
	
}