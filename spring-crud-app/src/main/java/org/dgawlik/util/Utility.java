package org.dgawlik.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Utility {

    @NotNull
    public static List<String> getWithDefaultWithAppended(List<String> cases, String id) {
        if (cases == null)
            cases = new ArrayList<>();
        else
            cases = new ArrayList<>(cases);
        cases.add(id);
        return cases;
    }
}
