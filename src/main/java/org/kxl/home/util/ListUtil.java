package org.kxl.home.util;

import java.util.ArrayList;
import java.util.List;

public class ListUtil {

    public static <T> List<T> reverse(List<T> list) {
        List<T> result = new ArrayList<>();
        for (int i = list.size() - 1; i >= 0; i--) {
            result.add(list.get(i));
        }
        return result;
    }

}
