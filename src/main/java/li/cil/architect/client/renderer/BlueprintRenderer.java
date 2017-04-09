package li.cil.architect.client.renderer;

import li.cil.architect.common.init.Items;
import li.cil.architect.common.item.ItemBlueprint;
import li.cil.architect.common.item.data.BlueprintData;
import li.cil.architect.util.PlayerUtils;
import li.cil.architect.util.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import static li.cil.architect.client.renderer.OverlayRendererUtils.*;

public enum BlueprintRenderer {
    INSTANCE;

    @SubscribeEvent
    public void onWorldRender(final RenderWorldLastEvent event) {
        final Minecraft mc = Minecraft.getMinecraft();
        final EntityPlayer player = mc.player;
        final World world = mc.world;
        final ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
        if (stack.getItem() != Items.blueprint) {
            return;
        }

        doPositionPrologue(event);
        doOverlayPrologue();

        final BlueprintData data = ItemBlueprint.getData(stack);

        final BlockPos hitPos;
        final RayTraceResult hit = mc.objectMouseOver;
        if (hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK) {
            hitPos = hit.getBlockPos();
        } else {
            hitPos = PlayerUtils.getLookAtPos(player);
        }

        final float dt = computeScaleOffset();

        GlStateManager.color(0.2f, 0.9f, 0.4f, 0.5f);

        final AxisAlignedBB cellBounds = data.getCellBounds(hitPos);
        doWirePrologue();
        renderCube(cellBounds);
        doWireEpilogue();

        GlStateManager.color(0.2f, 0.4f, 0.9f, 0.15f);

        {
            final Tessellator t = Tessellator.getInstance();
            final VertexBuffer buffer = t.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

            data.getBlocks(hitPos).forEach(pos -> {
                if (WorldUtils.isReplaceable(world, pos)) {
                    drawCube(pos, buffer, dt);
                }
            });

            t.draw();
        }

        GlStateManager.color(0.9f, 0.2f, 0.2f, 0.3f);

        {
            final Tessellator t = Tessellator.getInstance();
            final VertexBuffer buffer = t.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

            data.getBlocks(hitPos).forEach(pos -> {
                if (!WorldUtils.isReplaceable(world, pos)) {
                    drawCube(pos, buffer, dt);
                }
            });

            t.draw();
        }

        doOverlayEpilogue();
        doPositionEpilogue();
    }
}
