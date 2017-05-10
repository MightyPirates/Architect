package li.cil.architect.client.renderer;

import li.cil.architect.common.init.Items;
import li.cil.architect.common.item.AbstractProvider;
import li.cil.architect.util.ItemStackUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import static li.cil.architect.client.renderer.OverlayRendererUtils.*;

public enum ProviderRenderer {
    INSTANCE;

    private static final float SIDE_INSET = 0.2f;

    @SubscribeEvent
    public void onWorldRender(final RenderWorldLastEvent event) {
        final Minecraft mc = Minecraft.getMinecraft();
        final EntityPlayer player = mc.player;

        final ItemStack stack = Items.getHeldItem(player, Items::isProvider);
        if (ItemStackUtils.isEmpty(stack)) {
            return;
        }

        doPositionPrologue(event);
        doOverlayPrologue();

        final float dt = computeScaleOffset();

        final RayTraceResult hit = mc.objectMouseOver;
        if (hit != null) {
            if (hit.typeOfHit == RayTraceResult.Type.BLOCK) {
                final BlockPos hitPos = hit.getBlockPos();
                final TileEntity tileEntity = mc.world.getTileEntity(hitPos);
                if (tileEntity != null && ((AbstractProvider) stack.getItem()).isValidTarget(tileEntity, hit.sideHit)) {
                    final EnumFacing side = hit.sideHit;

                    doWirePrologue();
                    GlStateManager.color(0.2f, 0.9f, 0.4f, 0.4f);
                    renderCubePulsing(hitPos, dt);
                    GlStateManager.color(0.2f, 0.9f, 0.4f, 0.8f);
                    renderSide(hitPos, side);
                    doWireEpilogue();
                }
            } else if (hit.typeOfHit == RayTraceResult.Type.ENTITY) {
                final Entity entity = hit.entityHit;
                if (entity != null && ((AbstractProvider) stack.getItem()).isValidTarget(entity)) {

                    doWirePrologue();
                    GlStateManager.color(0.2f, 0.9f, 0.4f, 1f);
                    renderEntitySelectorWire(entity, dt);
                    doWireEpilogue();
                }
            }
        }

        if (AbstractProvider.isBoundToBlock(stack)) {
            if (AbstractProvider.getDimension(stack) == player.getEntityWorld().provider.getDimension()) {
                final BlockPos pos = AbstractProvider.getPosition(stack);
                if (player.getDistanceSq(pos) <= 64 * 64) {
                    final EnumFacing side = AbstractProvider.getSide(stack);

                    GlStateManager.color(0.2f, 0.9f, 0.4f, 0.4f);
                    renderCubePulsing(pos, dt);
                    GlStateManager.color(0.2f, 0.9f, 0.4f, 0.8f);
                    renderSide(pos, side);
                }
            }
        } else if (AbstractProvider.isBoundToEntity(stack)) {
            if (AbstractProvider.getDimension(stack) == player.getEntityWorld().provider.getDimension()) {
                Entity entity = AbstractProvider.getEntity(stack, mc.world);
                if (entity != null && player.getDistanceSqToEntity(entity) <= 64 * 64) {

                    GlStateManager.color(0.2f, 0.9f, 0.4f, 0.4f);
                    renderEntitySelector(entity, dt);
                }
            }
        }

        doOverlayEpilogue();
        doPositionEpilogue();
    }

    private static void renderSide(final BlockPos pos, final EnumFacing side) {
        GlStateManager.disableCull();

        final Tessellator t = Tessellator.getInstance();
        final VertexBuffer buffer = t.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        final AxisAlignedBB bounds = new AxisAlignedBB(pos);
        switch (side) {
            case DOWN:
                drawPlaneNegY(bounds.minY, bounds.minX + SIDE_INSET, bounds.maxX - SIDE_INSET, bounds.minZ + SIDE_INSET, bounds.maxZ - SIDE_INSET, buffer);
                break;
            case UP:
                drawPlanePosY(bounds.maxY, bounds.minX + SIDE_INSET, bounds.maxX - SIDE_INSET, bounds.minZ + SIDE_INSET, bounds.maxZ - SIDE_INSET, buffer);
                break;
            case NORTH:
                drawPlaneNegZ(bounds.minZ, bounds.minX + SIDE_INSET, bounds.maxX - SIDE_INSET, bounds.minY + SIDE_INSET, bounds.maxY - SIDE_INSET, buffer);
                break;
            case SOUTH:
                drawPlanePosZ(bounds.maxZ, bounds.minX + SIDE_INSET, bounds.maxX - SIDE_INSET, bounds.minY + SIDE_INSET, bounds.maxY - SIDE_INSET, buffer);
                break;
            case WEST:
                drawPlaneNegX(bounds.minX, bounds.minY + SIDE_INSET, bounds.maxY - SIDE_INSET, bounds.minZ + SIDE_INSET, bounds.maxZ - SIDE_INSET, buffer);
                break;
            case EAST:
                drawPlanePosX(bounds.maxX, bounds.minY + SIDE_INSET, bounds.maxY - SIDE_INSET, bounds.minZ + SIDE_INSET, bounds.maxZ - SIDE_INSET, buffer);
                break;
        }

        t.draw();

        GlStateManager.enableCull();
    }
}
