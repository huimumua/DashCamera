package com.askey.dvr.cdr7010.dashcam.util;

import java.util.EnumSet;

public class SetUtils {
    public static boolean equals(EnumSet<?> set1, EnumSet<?> set2) {
        if (set1 == null || set2 == null) {
            return false;
        }
        if (set1.size() != set2.size()) {
            return false;
        }
        return set1.containsAll(set2);
    }
}
