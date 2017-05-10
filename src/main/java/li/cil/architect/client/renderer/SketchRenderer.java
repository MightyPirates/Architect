package li.cil.architect.client.renderer;

import li.cil.architect.api.ConverterAPI;
import li.cil.architect.common.init.Items;
import li.cil.architect.common.item.ItemSketch;
import li.cil.architect.common.item.data.SketchData;
import li.cil.architect.util.ItemStackUtils;
import li.cil.architect.util.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.stream.Stream;

import static li.cil.architect.client.renderer.OverlayRendererUtils.*;

public enum SketchRenderer {
    INSTANCE;

    private static final float SELECTION_GROWTH = 0.05f;

    // Cached data to avoid having to deserialize it from NBT each frame.
    private static WeakReference<ItemStack> lastStack;
    private static SketchData lastData;

    @SubscribeEvent
    public void onWorldRender(final RenderWorldLastEvent event) {
        final Minecraft mc = Minecraft.getMinecraft();
        final EntityPlayer player = mc.player;

        final ItemStack stack = Items.getHeldItem(player, Items::isSketch);
        if (ItemStackUtils.isEmpty(stack)) {
            lastStack = null;
            lastData = null;
            return;
        }

        final ItemStack previousStack = lastStack != null ? lastStack.get() : null;
        if (previousStack == null || !ItemStack.areItemStackTagsEqual(previousStack, stack)) {
            lastStack = new WeakReference<>(stack);
            lastData = ItemSketch.getData(stack);
        }
        assert lastData != null;

        final SketchData data = lastData;
        //noinspection ConstantConditions !isEmpty guarantees non-null.
        if (!data.isEmpty() && player.getDistanceSq(data.getOrigin()) > 64 * 64) {
            return;
        }

        final boolean hasRangeSelection = ItemSketch.hasRangeSelection(stack);
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

        doPositionPrologue(event);
        doOverlayPrologue();

        final float dt = computeScaleOffset();

        if (!data.isEmpty()) {
            GlStateManager.color(0.2f, 0.4f, 0.9f, 0.5f);
            renderBlocks(data.getBlocks(), dt);

            GlStateManager.color(0.4f, 0.7f, 0.4f, 0.3f);
            renderCubeGrid(potentialBounds);
        }

        if (hitPos != null) {
            if (hasRangeSelection) {
                renderRangeSelection(player, ItemSketch.getRangeSelection(stack, hitPos));
            } else {
                renderBlockSelection(player, hitPos, potentialBounds);
            }
        }

        if (hitPos != null && data.isSet(hitPos)) {
            GlStateManager.color(0.2f, 0.4f, 0.9f, 0.3f);
            renderCubePulsing(hitPos, dt);

            doWirePrologue();
            GlStateManager.color(0.2f, 0.4f, 0.9f, 0.5f);
            renderCubePulsing(hitPos, dt);
            doWireEpilogue();
        }

        doOverlayEpilogue();
        doPositionEpilogue();
    }

    @SubscribeEvent
    public void onOverlayRender(final RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        final Minecraft mc = Minecraft.getMinecraft();
        final EntityPlayer player = mc.player;
        if (player.isSneaking()) {
            final ItemStack stack = Items.getHeldItem(player, Items::isSketch);
            if (ItemStackUtils.isEmpty(stack)) {
                return;
            }

            final ScaledResolution resolution = event.getResolution();
            renderShiftOverlay(player, resolution.getScaledWidth(), resolution.getScaledHeight(), event.getPartialTicks(), true);
        }
    }

    private static void renderBlockSelection(final EntityPlayer player, final BlockPos pos, final AxisAlignedBB potentialBounds) {
        final AxisAlignedBB bounds = new AxisAlignedBB(pos);
        if (player.isSneaking()) {
            if (potentialBounds.intersectsWith(bounds)) {
                GlStateManager.color(0.2f, 0.9f, 0.4f, 0.5f);
            } else {
                GlStateManager.color(0.9f, 0.4f, 0.2f, 0.5f);
            }
            renderCubeWire(pos, MIN - SELECTION_GROWTH, MAX + SELECTION_GROWTH);
        } else {
            if (potentialBounds.intersectsWith(bounds) && ConverterAPI.canSerialize(player.getEntityWorld(), pos)) {
                GlStateManager.color(0.2f, 0.9f, 0.4f, 0.5f);
            } else {
                GlStateManager.color(0.9f, 0.4f, 0.2f, 0.5f);
            }
            renderCube(pos, MIN - SELECTION_GROWTH, MAX + SELECTION_GROWTH);
        }
    }

    private static void renderRangeSelection(final EntityPlayer player, @Nullable final AxisAlignedBB bounds) {
        if (bounds == null) {
            return;
        }

        if (player.isSneaking()) {
            GlStateManager.color(0.9f, 0.4f, 0.2f, 0.2f);
        } else {
            GlStateManager.color(0.2f, 0.9f, 0.4f, 0.2f);
        }
        renderCube(bounds);

        if (player.isSneaking()) {
            GlStateManager.color(0.9f, 0.4f, 0.2f, 0.7f);
        } else {
            GlStateManager.color(0.2f, 0.9f, 0.4f, 0.7f);
        }
        renderCubeWire(bounds);
    }

    private static void renderBlocks(final Stream<BlockPos> blocks, final float dt) {
        final Tessellator t = Tessellator.getInstance();
        final VertexBuffer buffer = t.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        blocks.forEach(pos -> drawCube(pos, buffer, dt));

        t.draw();
    }
}
