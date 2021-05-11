package technicianlp.reauth;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class Utils {
    public static void copyString(String copy){
        StringSelection stringSelection = new StringSelection(copy);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }
    public static void alignButtons(int containerWidth,GuiButton button1,GuiButton button2){
        button1.x = containerWidth  / 2 - 50-53;
        button2.x = containerWidth / 2 - 50+53;

        button1.setWidth(100);
        button2.setWidth(100);
        button2.y = button1.y;
    }
    public static void alignButtons(int containerWidth,GuiButton button1,GuiButton button2,GuiButton button3){
        button1.x = containerWidth / 2 - 156;
        button2.x = containerWidth / 2 - 53;
        button3.x = containerWidth/ 2 + 50;

        button1.setWidth(100);
        button2.setWidth(100);
        button3.setWidth(100);

        button2.y = button1.y;
        button3.y = button1.y;
    }
}
