package technicianlp.reauth;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

public class GuiImageButton extends GuiButton {
    ResourceLocation image;
    public GuiImageButton(int id, int posX, int posY, String text,ResourceLocation image) {
        super(id, posX, posY, text);
        this.image = image;
    }

    public GuiImageButton(int id, int posX, int posY, int Width, int Height, String text, ResourceLocation image) {
        super(id, posX, posY, Width, Height, text);
        this.image = image;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float p_drawButton_4_) {
        super.drawButton(mc, mouseX, mouseY, p_drawButton_4_);
        mc.getTextureManager().bindTexture(image);
        GuiImageButton.drawModalRectWithCustomSizedTexture(this.x+3,this.y+3,0,0,this.width-6,this.height-6,this.width-6,this.height-6);
    }
}
