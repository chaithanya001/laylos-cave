package com.heynaveed.layloscave.keys;


public enum SpriteKeys {
    KIRK("kirk"),
    JINI("jini"),
    PORTAL("portal");

    private final String key;

    SpriteKeys(String key){
        this.key = key;
    }

    public boolean equalsKey(String otherKey){
        return key == otherKey;
    }

    public String getKey(){
        return key;
    }
}
