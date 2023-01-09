package net.pooleaf.sql;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class SqlColumn {
	
	private final String name, type;
	
	private final boolean notNull, autoIncrement;
	
	@Setter
	private boolean primaryKey;

}