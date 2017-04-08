package li.cil.architect.client.renderer;

import li.cil.architect.common.blueprint.JobManagerClient;
import li.cil.architect.common.init.Items;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import static li.cil.architect.client.renderer.OverlayRendererUtils.*;

public enum JobRenderer {
    INSTANCE;

    @SubscribeEvent
    public void onWorldRender(final RenderWorldLastEvent event) {
        final Minecraft mc = Minecraft.getMinecraft();
        final EntityPlayer player = mc.player;
        final ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
        if (stack.getItem() == Items.blueprint || stack.getItem() == Items.provider) {
            doPositionPrologue(event);
            doOverlayPrologue();

            final float dt = computeScaleOffset();

            GlStateManager.color(0.2f, 0.4f, 0.9f, 0.15f);

            doWirePrologue();

            final Tessellator t = Tessellator.getInstance();
            final VertexBuffer buffer = t.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

            JobManagerClient.INSTANCE.forEachJob(pos -> drawCube(pos, buffer, dt));

            t.draw();

            doWireEpilogue();

            doOverlayEpilogue();
            doPositionEpilogue();
        }
    }
}
