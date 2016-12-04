package com.heynaveed.layloscave.utils.maps;

/**
 * Created by naveed.shihab on 16/11/2016.
 */

public class PathDirection {

    enum Hub {
        UP(-1, 0), DOWN(1, 0), LEFT(0, -1), RIGHT(0, 1), NONE(0, 0);

        public final int x;
        public final int y;

        Hub(int x, int y){
            this.x = x;
            this.y = y;
        }
    }

    enum Cavern{
        UP(-4), DOWN(4), LEFT(-1), RIGHT(1), NONE(0);

        public final int direction;

        Cavern(int direction){ this.direction = direction; }
    }
}
