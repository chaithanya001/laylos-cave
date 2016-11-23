package com.heynaveed.layloscave.keys;


public enum CharacterKey {
    KIRK("kirk"),
    JINI("jini");

    private final String key;

    CharacterKey(String key){
        this.key = key;
    }

    public boolean equalsKey(String otherKey){
        return key == otherKey;
    }

    public String getKey(){
        return key;
    }
}
