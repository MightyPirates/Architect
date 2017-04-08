package li.cil.architect.client.renderer;

import li.cil.architect.api.BlueprintAPI;
import li.cil.architect.common.init.Items;
import li.cil.architect.common.item.ItemSketch;
import li.cil.architect.common.item.data.SketchData;
import li.cil.architect.util.PlayerUtils;
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
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import static li.cil.architect.client.renderer.OverlayRendererUtils.*;

public enum SketchRenderer {
    INSTANCE;

    private static final float SELECTION_GROWTH = 0.05f;

    @SubscribeEvent
    public void onWorldRender(final RenderWorldLastEvent event) {
        final Minecraft mc = Minecraft.getMinecraft();
        final EntityPlayer player = mc.player;
        final ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
        if (stack.getItem() == Items.sketch) {
            doPositionPrologue(event);
            doOverlayPrologue();

            final boolean hasRangeSelection = ItemSketch.hasRangeSelection(stack);
            final SketchData data = ItemSketch.getData(stack);
            final AxisAlignedBB potentialBounds = data.getPotentialBounds();

            final BlockPos hitPos;
            final RayTraceResult hit = mc.objectMouseOver;
            if (hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK) {
                hitPos = hit.getBlockPos();
            } else if (player.isSneaking() || hasRangeSelection) {
                hitPos = PlayerUtils.getLookAtPos(player);
            } else {
                hitPos = null;
            }

            if (hitPos != null) {
                if (hasRangeSelection) {
                    final AxisAlignedBB rangeBounds = ItemSketch.getRangeSelection(stack, hitPos);
                    if (rangeBounds != null) {
                        if (player.isSneaking()) {
                            GlStateManager.color(0.9f, 0.4f, 0.2f, 0.2f);
                        } else {
                            GlStateManager.color(0.2f, 0.9f, 0.4f, 0.2f);
                        }
                        renderCube(rangeBounds);
                        if (player.isSneaking()) {
                            GlStateManager.color(0.9f, 0.4f, 0.2f, 0.7f);
                        } else {
                            GlStateManager.color(0.2f, 0.9f, 0.4f, 0.7f);
                        }
                        renderCubeWire(rangeBounds);
                    }
                } else {
                    final AxisAlignedBB hitBounds = new AxisAlignedBB(hitPos);
                    if (player.isSneaking()) {
                        if (potentialBounds.intersectsWith(hitBounds)) {
                            GlStateManager.color(0.2f, 0.9f, 0.4f, 0.5f);
                        } else {
                            GlStateManager.color(0.9f, 0.4f, 0.2f, 0.5f);
                        }
                        renderCubeWire(hitPos, MIN - SELECTION_GROWTH, MAX + SELECTION_GROWTH);
                    } else {
                        if (potentialBounds.intersectsWith(hitBounds) && BlueprintAPI.canSerialize(mc.world, hitPos)) {
                            GlStateManager.color(0.2f, 0.9f, 0.4f, 0.5f);
                        } else {
                            GlStateManager.color(0.9f, 0.4f, 0.2f, 0.5f);
                        }
                        renderCube(hitPos, MIN - SELECTION_GROWTH, MAX + SELECTION_GROWTH);
                    }
                }
            }

            if (!data.isEmpty()) {
                GlStateManager.color(0.4f, 0.7f, 0.9f, 1f);

                renderCubeGrid(potentialBounds);

                final float dt = computeScaleOffset();

                {
                    GlStateManager.color(0.2f, 0.4f, 0.9f, 0.15f);

                    final Tessellator t = Tessellator.getInstance();
                    final VertexBuffer buffer = t.getBuffer();
                    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

                    data.getBlocks().forEach(pos -> drawCube(pos, buffer, dt));

                    t.draw();
                }

                if (hitPos != null && data.isSet(hitPos)) {
                    {
                        GlStateManager.color(0.2f, 0.4f, 0.9f, 0.3f);

                        final Tessellator t = Tessellator.getInstance();
                        final VertexBuffer buffer = t.getBuffer();
                        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

                        drawCube(hitPos, buffer, dt);

                        t.draw();
                    }
                    {
                        GlStateManager.color(0.2f, 0.4f, 0.9f, 0.5f);

                        doWirePrologue();

                        final Tessellator t = Tessellator.getInstance();
                        final VertexBuffer buffer = t.getBuffer();
                        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

                        drawCube(hitPos, buffer, dt);

                        t.draw();

                        doWireEpilogue();
                    }
                }
            }

            doOverlayEpilogue();
            doPositionEpilogue();
        }
    }
}
