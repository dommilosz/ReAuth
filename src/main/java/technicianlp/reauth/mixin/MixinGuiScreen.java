package technicianlp.reauth.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import technicianlp.reauth.IGuiScreen;

@Mixin(GuiScreen.class)
public abstract class MixinGuiScreen implements IGuiScreen {

    @Shadow
    protected abstract <T extends GuiButton> T addButton(T button);

    @Override
    public <T extends GuiButton> T doAddButton(T button) {
        return addButton(button);
    }

}
