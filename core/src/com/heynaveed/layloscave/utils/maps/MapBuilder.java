package com.heynaveed.layloscave.utils.maps;


public interface MapBuilder {

    void initialiseWorkingValues();
    void initialiseTileIDSet();
    void generateTerrain();
    void populateTileIDSet();
}
