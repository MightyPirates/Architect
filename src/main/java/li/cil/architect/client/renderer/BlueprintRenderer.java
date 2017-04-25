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
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.lang.ref.WeakReference;
import java.util.stream.Stream;

import static li.cil.architect.client.renderer.OverlayRendererUtils.*;

public enum BlueprintRenderer {
    INSTANCE;

    private static final float ROTATION_INSET = 0.2f;

    // Cached data to avoid having to deserialize it from NBT each frame.
    private static WeakReference<ItemStack> lastStack;
    private static BlueprintData lastData;

    @SubscribeEvent
    public void onWorldRender(final RenderWorldLastEvent event) {
        final Minecraft mc = Minecraft.getMinecraft();
        final EntityPlayer player = mc.player;
        final World world = mc.world;

        final ItemStack stack = Items.getHeldItem(player, Items::isBlueprint);
        if (stack.isEmpty()) {
            lastStack = null;
            lastData = null;
            return;
        }

        final ItemStack previousStack = lastStack != null ? lastStack.get() : null;
        if (previousStack == null || !ItemStack.areItemStackTagsEqual(previousStack, stack)) {
            lastStack = new WeakReference<>(stack);
            lastData = ItemBlueprint.getData(stack);
        }
        assert lastData != null;

        final BlueprintData data = lastData;
        if (data.isEmpty()) {
            return;
        }

        final BlockPos hitPos;
        final RayTraceResult hit = mc.objectMouseOver;
        if (hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK) {
            hitPos = hit.getBlockPos();
        } else {
            hitPos = PlayerUtils.getLookAtPos(player);
        }

        final float dt = computeScaleOffset();
        final AxisAlignedBB cellBounds = data.getCellBounds(hitPos);

        doPositionPrologue(event);
        doOverlayPrologue();

        GlStateManager.color(0.2f, 0.4f, 0.9f, 0.15f);
        renderValidBlocks(world, data.getBlocks(hitPos), dt);

        GlStateManager.color(0.9f, 0.2f, 0.2f, 0.3f);
        renderInvalidBlocks(world, data.getBlocks(hitPos), dt);

        GlStateManager.color(0.2f, 0.9f, 0.4f, 0.2f);
        renderCellBounds(cellBounds);
        renderRotationIndicator(data.getRotation(), cellBounds);

        doOverlayEpilogue();
        doPositionEpilogue();
    }

    private static void renderCellBounds(final AxisAlignedBB cellBounds) {
        doWirePrologue();
        renderCube(cellBounds);
        doWireEpilogue();
    }

    private static void renderRotationIndicator(final Rotation rotation, final AxisAlignedBB cellBounds) {
        GlStateManager.disableCull();
        GlStateManager.pushMatrix();

        GlStateManager.translate(cellBounds.minX, cellBounds.minY, cellBounds.minZ);
        GlStateManager.scale(cellBounds.maxX - cellBounds.minX, 1, cellBounds.maxZ - cellBounds.minZ);
        GlStateManager.translate(0.5, 0, 0.5);
        GlStateManager.rotate(rotation.ordinal() * 90, 0, -1, 0);
        GlStateManager.translate(-0.5, 0, -0.5);

        final Tessellator t = Tessellator.getInstance();
        final VertexBuffer buffer = t.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION);

        buffer.pos(0, 0, 0.5).endVertex();
        buffer.pos(0.5, 0, 1).endVertex();
        buffer.pos(1, 0, 0.5).endVertex();
        buffer.pos(0.25, 0, 0).endVertex();
        buffer.pos(0.25, 0, 0.5).endVertex();
        buffer.pos(0.75, 0, 0.5).endVertex();
        buffer.pos(0.25, 0, 0).endVertex();
        buffer.pos(0.75, 0, 0.5).endVertex();
        buffer.pos(0.75, 0, 0).endVertex();

        t.draw();

        GlStateManager.popMatrix();
        GlStateManager.enableCull();
    }

    private static void renderValidBlocks(final World world, final Stream<BlockPos> blocks, final float dt) {
        final Tessellator t = Tessellator.getInstance();
        final VertexBuffer buffer = t.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        blocks.forEach(pos -> {
            if (WorldUtils.isReplaceable(world, pos)) {
                drawCube(pos, buffer, dt);
            }
        });

        t.draw();
    }

    private static void renderInvalidBlocks(final World world, final Stream<BlockPos> blocks, final float dt) {
        final Tessellator t = Tessellator.getInstance();
        final VertexBuffer buffer = t.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        blocks.forEach(pos -> {
            if (!WorldUtils.isReplaceable(world, pos)) {
                drawCube(pos, buffer, dt);
            }
        });

        t.draw();
    }
}
