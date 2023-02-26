package net.pooleaf.sql;

import com.zaxxer.hikari.HikariDataSource;
import kr.codingtree.mcsi.MCServerInfo;
import lombok.Cleanup;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AbstractSqlManager {

	protected static MCServerInfo mcsi = MCServerInfo.getInstance();

	private HikariDataSource dataSource;

	
	public boolean connect() {
		try {
			dataSource = new HikariDataSource(mcsi.getSqlConfig().getHikariConfig());

			onConnected();

			mcsi.getConsole().log((mcsi.getSqlConfig().getType() > 0 ? "MariaDB" : "MySQL") + "에 연결되었습니다.");
		} catch(Exception e) {
			e.printStackTrace();

			mcsi.getConsole().log((mcsi.getSqlConfig().getType() > 0 ? "MariaDB" : "MySQL") + "에 연결할 수 없습니다.");
			return false;
		}

		return true;
	}
	
	public void close() {
		if(dataSource == null) return;

		try {
			dataSource.close();
			
			mcsi.getConsole().log((mcsi.getSqlConfig().getType() > 0 ? "MariaDB" : "MySQL") + "과의 연결을 종료했습니다.");
		} catch(Exception e) {
			e.printStackTrace();
			
			mcsi.getConsole().log((mcsi.getSqlConfig().getType() > 0 ? "MariaDB" : "MySQL") + "과의 연결을 종료하는 중 오류가 발생했습니다.");
		}
	}

	public void onConnected() { }

	public boolean isConnected() {
		return dataSource != null && !dataSource.isClosed();
	}
	
	@SneakyThrows(SQLException.class)
	public void update(String sql) {
		sql = sql.replace("\\", "\\\\");

		mcsi.getConsole().debugLog("SQL Update: " + sql);

		@Cleanup PreparedStatement state = getPreparedStatement(sql);
		state.executeUpdate();
	}

	public void update(String sql, Object...args) {
		update(String.format(sql, args));
	}
	
	@SneakyThrows(SQLException.class)
	public PreparedStatement getPreparedStatement(String sql) {
		mcsi.getConsole().debugLog("SQL PreparedStatement: " + sql);
		return new LPreparedStatement(dataSource.getConnection().prepareStatement(sql));
	}
	
	public PreparedStatement getPreparedStatement(String sql, Object...args) {
		return getPreparedStatement(String.format(sql, args));
	}
	
	public void createTable(String name, String column) {
		update("create table if not exists " + name + " (" + column + ")");
	}
	
	public void deleteTable(String name) {
		update("drop table " + name);
	}
	
	public void truncateTable(String name) {
		update("truncate table " + name);
	}
	
}