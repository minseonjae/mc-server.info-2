package kr.codingtree.mcsi.sql;

import com.zaxxer.hikari.HikariConfig;
import kr.codingtree.fieldconfig.annotation.ConfigName;
import lombok.Data;

@Data
public class SqlConfig {

    @ConfigName("종류")
    private int type = 0;

    @ConfigName("주소")
    private String address = "localhost";

    @ConfigName("포트")
    private int port = 3306;

    @ConfigName("데이터베이스")
    private String database = "database";

    @ConfigName("사용자")
    private String user = "user";

    @ConfigName("비밀번호")
    private String password = "user";

    @ConfigName("속성")
    private String properties = "?autoReconnect=true&useUnicode=true&characterEncoding=utf8";

    public HikariConfig getHikariConfig() {
        HikariConfig config = new HikariConfig();

        switch (type) {
            case 0:
                // MySQL
                config.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s%s", address, port, database, properties));
                config.setUsername(user);
                config.setPassword(password);
                break;
            case 1:
                // MariaDB
                config.setJdbcUrl(String.format("jdbc:mariadb://%s:%d/%s%s", address, port, database, properties));
                config.setUsername(user);
                config.setPassword(password);
                break;
        }

        return config;
    }
}
