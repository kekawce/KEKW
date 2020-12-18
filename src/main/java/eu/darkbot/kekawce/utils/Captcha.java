package eu.darkbot.kekawce.utils;

import com.github.manolo8.darkbot.core.entities.Box;

import java.util.List;

public class Captcha {
    public static boolean exists(List<Box> boxes) {
        return boxes.stream().anyMatch(box -> box.type.equals("POISON_PUSAT_BOX_BLACK") || box.type.equals("BONUS_BOX_RED"));
    }
}
