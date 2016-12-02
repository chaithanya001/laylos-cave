package com.heynaveed.layloscave.keys;


public final class AnimationKey {

    public enum Kirk {
        STANDING(0), RUNNING(1), JUMPING(2), MUDDY_RUNNING(3), BOUNCE_JUMP(4), SLIDING(5);

        public final int index;

        Kirk(int index) {
            this.index = index;
        }
    }

    public enum Jini{
        FLOATING(0), FLYING(1), TELEPORTING(2), DOUBLE_JUMP(3);

        public final int index;

        Jini(int index) { this.index = index; }
    }
}
