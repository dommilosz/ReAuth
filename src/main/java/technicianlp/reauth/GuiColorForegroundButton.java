package technicianlp.reauth;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiColorForegroundButton extends GuiButton {
    int color = 0xFFFFFFFF;
    public GuiColorForegroundButton(int id, int posX, int posY, String text,int color) {
        super(id, posX, posY, text);
        this.color = color;
    }

    public GuiColorForegroundButton(int id, int posX, int posY, int Width, int Height, String text, int color) {
        super(id, posX, posY, Width, Height, text);
        this.color = color;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float p_drawButton_4_) {
        if (this.visible) {
            FontRenderer lvt_5_1_ = mc.fontRenderer;
            mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int lvt_6_1_ = this.getHoverState(this.hovered);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            this.drawTexturedModalRect(this.x, this.y, 0, 46 + lvt_6_1_ * 20, this.width / 2, this.height);
            this.drawTexturedModalRect(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + lvt_6_1_ * 20, this.width / 2, this.height);
            this.mouseDragged(mc, mouseX, mouseY);
            int textColor = color;
            if (!this.enabled) {
                textColor = 10526880;
            }

            this.drawCenteredString(lvt_5_1_, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, textColor);
        }
    }
}
