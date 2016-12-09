package com.heynaveed.layloscave.utils.maps;


interface MapBuilder {

    void initialiseWorkingValues();
    void initialiseTileIDSet();
    void generateTerrain();
    void populateTileIDSet();
}
