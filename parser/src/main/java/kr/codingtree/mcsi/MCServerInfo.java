package kr.codingtree.mcsi;

import kr.codingtree.console.Console;
import kr.codingtree.fieldconfig.FieldConfig;
import kr.codingtree.mcsi.sql.SqlConfig;
import kr.codingtree.platformconfig.YamlConfig;
import lombok.Getter;
import lombok.Setter;

public class MCServerInfo {

    private static MCServerInfo instance;

    public static MCServerInfo getInstance() {
        if (instance == null) {
            instance = new MCServerInfo();
        }

        return instance;
    }

    @Getter
    @Setter
    private Console console;

    @Getter
    private SqlConfig sqlConfig = new SqlConfig();

    @Getter
    private FieldConfig fieldSqlConfig;

    public void loadSqlConfig() {
        if (fieldSqlConfig == null) {
            fieldSqlConfig = new FieldConfig("data-config.yml", sqlConfig, YamlConfig.class);
        }

        fieldSqlConfig.load();
    }

    @Getter
    @Setter
    private int timeout;


}
