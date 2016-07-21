package com.ardic.android.smartkettle;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 27.04.2016.
 */

public class PacketUtils {

    private static final String TAG = PacketUtils.class.getSimpleName();

    public static String[] packetHandler(String packet) {

        String[] values, state = null;

        // parse sensor values example :#|Checksum|+DHT11:|82.40|28.00|33.00|~
        if (packet.charAt(0) == '#') {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "Incoming Sensor Data Packet : " + packet);
            }
            values = new String[3];
            List<String> valuesList = split(packet, "\\|");
                        Log.i(TAG, "valuesList" + valuesList);

                       if (valuesList.size() == 7) {

                                 values[0] = valuesList.get(3);
                                 values[1] = valuesList.get(4);
                                 values[2] = valuesList.get(5);
                       }
                        return values;

        }

        return state;

    }

    public static List<String> split(final String source, final String delimeter) {

        List<String> parts = new ArrayList<String>();

        for (String string : source.split(delimeter)) {
            parts.add(string);
        }

        return parts;

    }

}
