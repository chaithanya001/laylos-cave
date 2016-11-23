package com.heynaveed.layloscave.utils;

public class RoundTo {

    /**
     * Round up to nearest.
     *
     * @param passednumber the passednumber
     * @param roundto the roundto
     * @return the float
     */
    public static float RoundUpToNearest(float passednumber, float roundto)
    {

        // 105.5 up to nearest 1 = 106
        // 105.5 up to nearest 10 = 110
        // 105.5 up to nearest 7 = 112
        // 105.5 up to nearest 100 = 200
        // 105.5 up to nearest 0.2 = 105.6
        // 105.5 up to nearest 0.3 = 105.6

        //if no round to then just pass original number back
        if (roundto == 0)
        {
            return passednumber;
        }
        else
        {
            return (float) (Math.ceil(passednumber / roundto) * roundto);
        }
    }

    /**
     * Round down to nearest.
     *
     * @param passednumber the passednumber
     * @param roundto the roundto
     * @return the float
     */
    public static float RoundDownToNearest(float passednumber, float roundto)
    {

        // 105.5 down to nearest 1 = 105
        // 105.5 down to nearest 10 = 100
        // 105.5 down to nearest 7 = 105
        // 105.5 down to nearest 100 = 100
        // 105.5 down to nearest 0.2 = 105.4
        // 105.5 down to nearest 0.3 = 105.3

        //if no round to then just pass original number back
        if (roundto == 0)
        {
            return passednumber;
        }
        else
        {
            return (float) (Math.floor(passednumber / roundto) * roundto);
        }
    }

    /**
     * Round to nearest.
     *
     * @param passednumber the passednumber
     * @param roundto the roundto
     * @return the float
     */
    public static float RoundToNearest(float passednumber, float roundto)
    {

        // 105.5 down to nearest 1 = 105
        // 105.5 down to nearest 10 = 100
        // 105.5 down to nearest 7 = 105
        // 105.5 down to nearest 100 = 100
        // 105.5 down to nearest 0.2 = 105.4
        // 105.5 down to nearest 0.3 = 105.3

        //if no round to then just pass original number back
        if (roundto == 0)
        {
            return passednumber;
        }
        else
        {
            return (float) (Math.round(passednumber / roundto) * roundto);
        }
    }

}
