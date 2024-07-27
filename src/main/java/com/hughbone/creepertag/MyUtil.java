package com.hughbone.creepertag;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class MyUtil {

    public static Text getRainbowText(String name) {
        StringBuilder rainbowName = new StringBuilder();
        Formatting[] colors = {
                Formatting.RED, Formatting.GOLD, Formatting.YELLOW, Formatting.GREEN,
                Formatting.AQUA, Formatting.BLUE, Formatting.LIGHT_PURPLE
        };
        for (int i = 0; i < name.length(); i++) {
            Formatting color = colors[i % colors.length];
            rainbowName.append(color).append(name.charAt(i));
        }
        return Text.literal(rainbowName.toString());
    }
}
