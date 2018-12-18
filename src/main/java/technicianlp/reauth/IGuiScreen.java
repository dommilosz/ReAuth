package technicianlp.reauth;

import net.minecraft.client.gui.GuiButton;

public interface IGuiScreen {

    <T extends GuiButton> T doAddButton(T button);

}
