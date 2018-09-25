package com.stayrascal.service.application.constraints;

import java.util.Arrays;

public enum StorageDirs {
    COMPS, THESAURUS, ITEMS;

    public static String[] names() {
        return Arrays.stream(values()).map(Enum::name).toArray(String[]::new);
    }
}
