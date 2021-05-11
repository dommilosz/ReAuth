package technicianlp.reauth;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public class GuiReadOnlyTextField extends GuiTextField {

    public GuiReadOnlyTextField(int id, FontRenderer fontRenderer, int x, int y, int w, int h) {
        super(id, fontRenderer, x, y, w, h);
    }

    @Override
    public void writeText(String p_writeText_1_) {

    }

    @Override
    public boolean textboxKeyTyped(char p_textboxKeyTyped_1_, int p_textboxKeyTyped_2_) {
        String prevText = this.getText();
        boolean response = super.textboxKeyTyped(p_textboxKeyTyped_1_,p_textboxKeyTyped_2_);
        if(!this.getText().equals(prevText)){
            this.setText(prevText);
        }
        return response;
    }
}
