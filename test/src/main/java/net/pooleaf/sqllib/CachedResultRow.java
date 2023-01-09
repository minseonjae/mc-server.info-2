package net.pooleaf.sqllib;

import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.Map;

@Getter
@ToString
public class CachedResultRow {

    private Map<String, Object> datas;


    protected CachedResultRow(Map<String, Object> datas) {
        this.datas = datas;
        Collections.unmodifiableMap(this.datas);
    }


    public boolean exists(String key) {
        return datas.containsKey(key);
    }

    public int size() {
        return datas.size();
    }

    public Object get(String key) {
        return datas.get(key);
    }

    public Object get(int index) {
        return datas.get(datas.keySet().toArray()[index]);
    }

    public String getString(String key) {
        return (String) get(key);
    }

    public String getString(int index) {
        return (String) get(index);
    }

    public Boolean getBoolean(String key) {
        return (Boolean) get(key);
    }

    public Boolean getBoolean(int index) {
        return (Boolean) get(index);
    }

    public Integer getInt(String key) {
        return (Integer) get(key);
    }

    public Integer getInt(int index) {
        return (Integer) get(index);
    }

    public Long getLong(String key) {
        return (Long) get(key);
    }

    public Long getLong(int index) {
        return (Long) get(index);
    }

    public Double getDouble(String key) {
        return (Double) get(key);
    }

    public Double getDouble(int index) {
        return (Double) get(index);
    }

}
