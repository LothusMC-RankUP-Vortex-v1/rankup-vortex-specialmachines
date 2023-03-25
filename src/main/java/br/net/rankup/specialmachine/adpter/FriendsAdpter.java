package br.net.rankup.specialmachine.adpter;

import java.util.ArrayList;
import java.util.List;

public class FriendsAdpter {

    public static List<String> deserialize(String string) {
        String stringReplaced = string.replace("[", "").replace("]", "").replace(" ", "");
        List<String> list = new ArrayList<>();
        if(!stringReplaced.equalsIgnoreCase("")) {
        String[] split = stringReplaced.split(",");
            for (int i = 0; i < split.length; i++) {
                list.add(split[i]);
            }
        }
        return list;
    }

}
