package io.inway.ringtone.player;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AlarmRingtone implements Serializable {
    private String name;
    private String uri;

    public AlarmRingtone(String name, String uri) {
        this.name = name;
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Map<String, Object> toMap() {
        final Map<String, Object> map = new HashMap<>();
        map.put("name", this.getName());
        map.put("uri", this.getUri());
        return map;
    }
}
