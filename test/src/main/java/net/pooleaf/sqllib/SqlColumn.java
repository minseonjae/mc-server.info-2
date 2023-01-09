package net.pooleaf.sqllib;

import lombok.Data;

@Data
public class SqlColumn {

    private String name;
    private String type;

    private boolean notNull;
    private boolean autoIncrement;
    private boolean primaryKey;

}
