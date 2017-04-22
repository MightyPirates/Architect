package li.cil.architect.client.gui;

import li.cil.architect.api.API;
import li.cil.architect.common.init.Items;
import li.cil.architect.common.item.ItemBlueprint;
import li.cil.architect.common.network.Network;
import li.cil.architect.common.network.message.MessageBlueprintData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

final class GuiBlueprint extends GuiScreen {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(API.MOD_ID, "textures/gui/blueprint.png");

    private final EntityPlayer player;
    private final ItemStack blueprint;

    private int xSize;
    private int ySize;
    private int guiLeft;
    private int guiTop;

    private GuiTextField textField;
    private GuiButtonColor color;

    GuiBlueprint(final EntityPlayer player, final ItemStack blueprint) {
        this.player = player;
        this.blueprint = blueprint;
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        drawDefaultBackground();
        mc.renderEngine.bindTexture(BACKGROUND);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        textField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(final char typedChar, final int keyCode) throws IOException {
        if (textField.textboxKeyTyped(typedChar, keyCode)) {
            return;
        }

        if (keyCode == Keyboard.KEY_RETURN) {
            Network.INSTANCE.getWrapper().sendToServer(new MessageBlueprintData(textField.getText(), color.getColor()));
            player.closeScreen();
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void actionPerformed(final GuiButton button) throws IOException {
        super.actionPerformed(button);
        color.cycleColor();
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);

        xSize = 176;
        ySize = 30;
        guiLeft = (width - xSize) / 2;
        guiTop = (height - ySize) / 2;

        final String oldText;
        if (textField != null) {
            oldText = textField.getText();
        } else {
            oldText = blueprint.getDisplayName();
        }
        textField = new GuiTextField(0, fontRendererObj, guiLeft + 6, guiTop + 6, 142, 18);
        textField.setMaxStringLength(24);
        textField.setCanLoseFocus(false);
        textField.setFocused(true);
        textField.setText(oldText);

        final EnumDyeColor oldColor;
        if (color != null) {
            oldColor = color.getColor();
        } else {
            oldColor = ItemBlueprint.getColor(blueprint);
        }
        color = new GuiButtonColor(0, guiLeft + 151, guiTop + 5, 20, 20);
        color.setColor(oldColor);

        addButton(color);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        final ItemStack stack = Items.getHeldItem(player, Items::isBlueprint);
        if (stack != blueprint) {
            player.closeScreen();
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private static final class GuiButtonColor extends GuiButton {
        private EnumDyeColor color = EnumDyeColor.WHITE;

        GuiButtonColor(final int buttonId, final int x, final int y, final int width, final int height) {
            super(buttonId, x, y, width, height, "");
        }

        EnumDyeColor getColor() {
            return color;
        }

        void setColor(final EnumDyeColor color) {
            this.color = color;
        }

        void cycleColor() {
            color = EnumDyeColor.byMetadata((color.getMetadata() + 1) % 16);
        }

        @Override
        public void drawButton(final Minecraft mc, final int mouseX, final int mouseY) {
            super.drawButton(mc, mouseX, mouseY);
            if (this.visible && color != null) {
                drawRect(xPosition + 4, yPosition + 4, xPosition + width - 4, yPosition + height - 4, 0xFF000000 | color.getMapColor().colorValue);
                GlStateManager.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
                final int borderColor;
                if (this.hovered) {
                    borderColor = 0xFFCCCCCC;
                } else {
                    borderColor = 0xFF999999;
                }
                drawRect(xPosition + 4, yPosition + 4, xPosition + width - 4, yPosition + height - 4, borderColor);
                GlStateManager.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
            }
        }
    }
}
