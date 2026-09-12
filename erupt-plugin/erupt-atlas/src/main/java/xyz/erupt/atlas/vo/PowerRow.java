package xyz.erupt.atlas.vo;

import java.util.List;

/**
 * What a model declares it can do, next to what the menu tree actually offers. The two drift:
 * function buttons are written once, when the menu is created, from the power flags as they stood
 * then — a flag flipped afterwards never grows a button, and the permission behind it is never
 * granted to anyone.
 *
 * @author YuePeng
 */
public record PowerRow(String model, String label, String module, String menuType,
                       List<String> power, List<String> buttons, String powerHandler) {
}
