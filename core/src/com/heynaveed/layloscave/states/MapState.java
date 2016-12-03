package com.heynaveed.layloscave.states;

/**
 * Created by naveed.shihab on 03/12/2016.
 */

public enum MapState {
    HUB("HubMap"), CAVERN("CavernMap");

    public final String name;

    MapState(String name){ this.name = name; }
}
