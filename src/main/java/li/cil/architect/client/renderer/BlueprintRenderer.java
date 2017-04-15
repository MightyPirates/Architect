package li.cil.architect.client.renderer;

import li.cil.architect.common.init.Items;
import li.cil.architect.common.item.ItemBlueprint;
import li.cil.architect.common.item.data.BlueprintData;
import li.cil.architect.util.ItemStackUtils;
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

import java.util.stream.Stream;

import static li.cil.architect.client.renderer.OverlayRendererUtils.*;

public enum BlueprintRenderer {
    INSTANCE;

    private static final float ROTATION_INSET = 0.2f;

    @SubscribeEvent
    public void onWorldRender(final RenderWorldLastEvent event) {
        final Minecraft mc = Minecraft.getMinecraft();
        final EntityPlayer player = mc.player;
        final World world = mc.world;

        final ItemStack stack = Items.getHeldItem(player, Items::isBlueprint);
        if (ItemStackUtils.isEmpty(stack)) {
            return;
        }

        final BlueprintData data = ItemBlueprint.getData(stack);
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

        GlStateManager.color(0.2f, 0.9f, 0.4f, 0.5f);
        renderCellBounds(cellBounds);
        renderRotationIndicator(data.getRotation(), cellBounds);

        GlStateManager.color(0.2f, 0.4f, 0.9f, 0.15f);
        renderValidBlocks(world, data.getBlocks(hitPos), dt);

        GlStateManager.color(0.9f, 0.2f, 0.2f, 0.3f);
        renderInvalidBlocks(world, data.getBlocks(hitPos), dt);

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

        final Tessellator t = Tessellator.getInstance();
        final VertexBuffer buffer = t.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        switch (rotation) {
            case NONE:
                drawPlaneNegZ(cellBounds.minZ, cellBounds.minX + ROTATION_INSET, cellBounds.maxX - ROTATION_INSET, cellBounds.minY + ROTATION_INSET, cellBounds.maxY - ROTATION_INSET, buffer);
                break;
            case CLOCKWISE_90:
                drawPlanePosX(cellBounds.maxX, cellBounds.minY + ROTATION_INSET, cellBounds.maxY - ROTATION_INSET, cellBounds.minZ + ROTATION_INSET, cellBounds.maxZ - ROTATION_INSET, buffer);
                break;
            case CLOCKWISE_180:
                drawPlanePosZ(cellBounds.maxZ, cellBounds.minX + ROTATION_INSET, cellBounds.maxX - ROTATION_INSET, cellBounds.minY + ROTATION_INSET, cellBounds.maxY - ROTATION_INSET, buffer);
                break;
            case COUNTERCLOCKWISE_90:
                drawPlaneNegX(cellBounds.minX, cellBounds.minY + ROTATION_INSET, cellBounds.maxY - ROTATION_INSET, cellBounds.minZ + ROTATION_INSET, cellBounds.maxZ - ROTATION_INSET, buffer);
                break;
        }

        t.draw();

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
