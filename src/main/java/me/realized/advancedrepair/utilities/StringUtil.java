package me.realized.advancedrepair.utilities;

import java.util.List;

public class StringUtil {

    public static String join(List<String> list) {
        if (list.isEmpty()) {
            return "none";
        }

        if (list.size() == 1) {
            return list.get(0);
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < list.size() - 1; i++) {
            builder.append(list.get(i)).append(", ");
        }

        builder.append(list.get(list.size() - 1));
        return builder.toString();
    }
}
