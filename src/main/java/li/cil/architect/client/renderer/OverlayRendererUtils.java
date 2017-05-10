package li.cil.architect.client.renderer;

import li.cil.architect.common.config.Settings;
import li.cil.architect.util.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

final class OverlayRendererUtils {
    static final float MIN = 0.1f;
    static final float MAX = 0.9f;
    private static final float TWO_PI = (float) (Math.PI * 2);
    private static final int SCALE_FREQUENCY = 0x7FF;
    private static final float SCALE_NORMALIZER = TWO_PI / SCALE_FREQUENCY;
    private static final float SCALE_STRENGTH = 0.025f;

    static void doPositionPrologue(final RenderWorldLastEvent event) {
        final EntityPlayer player = Minecraft.getMinecraft().player;
        final double px = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getPartialTicks();
        final double py = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getPartialTicks();
        final double pz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getPartialTicks();

        GlStateManager.pushMatrix();
        GlStateManager.translate(-px, -py, -pz);
    }

    static void doPositionEpilogue() {
        GlStateManager.popMatrix();
    }

    static void doOverlayPrologue() {
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
    }

    static void doOverlayEpilogue() {
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
    }

    static void doWirePrologue() {
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GlStateManager.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        GlStateManager.disableCull();
    }

    static void doWireEpilogue() {
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        GlStateManager.enableCull();
    }

    static float computeScaleOffset() {
        return (System.currentTimeMillis() & SCALE_FREQUENCY) * SCALE_NORMALIZER;
    }

    static void renderCube(final BlockPos pos, final float min, final float max) {
        final Tessellator t = Tessellator.getInstance();
        final VertexBuffer buffer = t.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        drawCube(pos.getX() + min, pos.getY() + min, pos.getZ() + min, pos.getX() + max, pos.getY() + max, pos.getZ() + max, buffer);

        t.draw();
    }

    static void renderCube(final AxisAlignedBB bounds) {
        final Tessellator t = Tessellator.getInstance();
        final VertexBuffer buffer = t.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        drawCube((int) bounds.minX, (int) bounds.minY, (int) bounds.minZ, (int) bounds.maxX, (int) bounds.maxY, (int) bounds.maxZ, buffer);

        t.draw();
    }

    static void renderCubeWire(final BlockPos pos, final float min, final float max) {
        doWirePrologue();

        final Tessellator t = Tessellator.getInstance();
        final VertexBuffer buffer = t.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        drawCube(pos.getX() + min, pos.getY() + min, pos.getZ() + min, pos.getX() + max, pos.getY() + max, pos.getZ() + max, buffer);

        t.draw();

        doWireEpilogue();
    }

    static void renderCubeWire(final AxisAlignedBB bounds) {
        doWirePrologue();

        final Tessellator t = Tessellator.getInstance();
        final VertexBuffer buffer = t.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        drawCube((int) bounds.minX, (int) bounds.minY, (int) bounds.minZ, (int) bounds.maxX, (int) bounds.maxY, (int) bounds.maxZ, buffer);

        t.draw();

        doWireEpilogue();
    }

    static void renderCubeGrid(final AxisAlignedBB bounds) {
        doWirePrologue();
        GlStateManager.enableDepth();

        final Tessellator t = Tessellator.getInstance();
        final VertexBuffer buffer = t.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        drawCubeGrid((int) bounds.minX, (int) bounds.minY, (int) bounds.minZ, (int) bounds.maxX, (int) bounds.maxY, (int) bounds.maxZ, buffer);

        t.draw();

        GlStateManager.disableDepth();
        doWireEpilogue();
    }

    static void renderCubePulsing(final BlockPos hitPos, final float dt) {
        final Tessellator t = Tessellator.getInstance();
        final VertexBuffer buffer = t.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        drawCube(hitPos, buffer, dt);

        t.draw();
    }

    static void renderShiftOverlay(final EntityPlayer player, final int width, final int height, final float partialTicks, final boolean ignoreGrid) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();

        GlStateManager.translate(0, -100, 0);
        GlStateManager.rotate(-50, 1, 0, 0);
        GlStateManager.translate(0, 100, 0);

        GlStateManager.translate(width / 2, height / 2, 200);
        GlStateManager.rotate(player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks, -1, 0, 0);
        GlStateManager.rotate(25, 1, 0, 0);

        if (!ignoreGrid && Settings.enablePlacementGrid) {
            GlStateManager.rotate(player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks, 0, 1, 0);

            final EnumFacing facing = PlayerUtils.getPrimaryFacing(player);
            switch (facing) {
                case DOWN:
                    GlStateManager.rotate(90, 1, 0, 0);
                    break;
                case UP:
                    GlStateManager.rotate(270, 1, 0, 0);
                    break;
                case NORTH:
                    GlStateManager.rotate(180, 0, 1, 0);
                    break;
                case WEST:
                    GlStateManager.rotate(270, 0, 1, 0);
                    break;
                case EAST:
                    GlStateManager.rotate(90, 0, 1, 0);
                    break;
            }
        }

        GlStateManager.scale(-1, -1, -1);
        GlStateManager.scale(25, 25, 25);

        final Tessellator t = Tessellator.getInstance();
        final VertexBuffer buffer = t.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);

        drawArrow3D(buffer);

        t.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    static void drawCube(final BlockPos pos, final VertexBuffer buffer, final float dt) {
        final float offset = (pos.getX() + pos.getY() + pos.getZ()) % TWO_PI;
        final float scale = SCALE_STRENGTH * MathHelper.sin(offset + dt);
        final float min = MIN - scale;
        final float max = MAX + scale;
        drawCube(pos.getX() + min, pos.getY() + min, pos.getZ() + min, pos.getX() + max, pos.getY() + max, pos.getZ() + max, buffer);
    }

    private static void drawCube(final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ, final VertexBuffer buffer) {
        drawPlaneNegX(minX, minY, maxY, minZ, maxZ, buffer);
        drawPlanePosX(maxX, minY, maxY, minZ, maxZ, buffer);
        drawPlaneNegY(minY, minX, maxX, minZ, maxZ, buffer);
        drawPlanePosY(maxY, minX, maxX, minZ, maxZ, buffer);
        drawPlaneNegZ(minZ, minX, maxX, minY, maxY, buffer);
        drawPlanePosZ(maxZ, minX, maxX, minY, maxY, buffer);
    }

    private static void drawCubeGrid(final int minX, final int minY, final int minZ, final int maxX, final int maxY, final int maxZ, final VertexBuffer buffer) {
        drawCube(minX, minY, minZ, maxX, maxY, maxZ, buffer);

        // FWIW, this would be much nicer, because it'd use a *lot* fewer polys,
        // but sadly due to how line rendering works, standing in one of those
        // quads would then lead to ugly lines floating in players' faces...
        /*
        for (int x = minX + 1; x < maxX; x++) {
            buffer.pos(x, minY, minZ).endVertex();
            buffer.pos(x, minY, maxZ).endVertex();
            buffer.pos(x, maxY, maxZ).endVertex();
            buffer.pos(x, maxY, minZ).endVertex();
        }

        for (int y = minY + 1; y < maxY; y++) {
            buffer.pos(minX, y, minZ).endVertex();
            buffer.pos(maxX, y, minZ).endVertex();
            buffer.pos(maxX, y, maxZ).endVertex();
            buffer.pos(minX, y, maxZ).endVertex();
        }

        for (int z = minZ + 1; z < maxZ; z++) {
            buffer.pos(minX, minY, z).endVertex();
            buffer.pos(minX, maxY, z).endVertex();
            buffer.pos(maxX, maxY, z).endVertex();
            buffer.pos(maxX, minY, z).endVertex();
        }
        */

        for (int x0 = minX + 1; x0 < maxX; x0 += 2) {
            final int x1 = x0 + 1;

            drawPlaneNegY(minY, x0, x1, minZ, maxZ, buffer);
            drawPlanePosY(maxY, x0, x1, minZ, maxZ, buffer);
            drawPlaneNegZ(minZ, x0, x1, minY, maxY, buffer);
            drawPlanePosZ(maxZ, x0, x1, minY, maxY, buffer);
        }

        for (int y0 = minY + 1; y0 < maxY; y0 += 2) {
            final int y1 = y0 + 1;

            drawPlaneNegX(minX, y0, y1, minZ, maxZ, buffer);
            drawPlanePosX(maxX, y0, y1, minZ, maxZ, buffer);
            drawPlaneNegZ(minZ, minX, maxX, y0, y1, buffer);
            drawPlanePosZ(maxZ, minX, maxX, y0, y1, buffer);
        }

        for (int z0 = minZ + 1; z0 < maxZ; z0 += 2) {
            final int z1 = z0 + 1;

            drawPlaneNegX(minX, minY, maxY, z0, z1, buffer);
            drawPlanePosX(maxX, minY, maxY, z0, z1, buffer);
            drawPlaneNegY(minY, minX, maxX, z0, z1, buffer);
            drawPlanePosY(maxY, minX, maxX, z0, z1, buffer);
        }
    }

    static void drawPlaneNegX(final double x, final double minY, final double maxY, final double minZ, final double maxZ, final VertexBuffer buffer) {
        buffer.pos(x, minY, minZ).endVertex();
        buffer.pos(x, minY, maxZ).endVertex();
        buffer.pos(x, maxY, maxZ).endVertex();
        buffer.pos(x, maxY, minZ).endVertex();
    }

    static void drawPlanePosX(final double x, final double minY, final double maxY, final double minZ, final double maxZ, final VertexBuffer buffer) {
        buffer.pos(x, minY, minZ).endVertex();
        buffer.pos(x, maxY, minZ).endVertex();
        buffer.pos(x, maxY, maxZ).endVertex();
        buffer.pos(x, minY, maxZ).endVertex();
    }

    static void drawPlaneNegY(final double y, final double minX, final double maxX, final double minZ, final double maxZ, final VertexBuffer buffer) {
        buffer.pos(minX, y, minZ).endVertex();
        buffer.pos(maxX, y, minZ).endVertex();
        buffer.pos(maxX, y, maxZ).endVertex();
        buffer.pos(minX, y, maxZ).endVertex();
    }

    static void drawPlanePosY(final double y, final double minX, final double maxX, final double minZ, final double maxZ, final VertexBuffer buffer) {
        buffer.pos(minX, y, minZ).endVertex();
        buffer.pos(minX, y, maxZ).endVertex();
        buffer.pos(maxX, y, maxZ).endVertex();
        buffer.pos(maxX, y, minZ).endVertex();
    }

    static void drawPlaneNegZ(final double z, final double minX, final double maxX, final double minY, final double maxY, final VertexBuffer buffer) {
        buffer.pos(minX, minY, z).endVertex();
        buffer.pos(minX, maxY, z).endVertex();
        buffer.pos(maxX, maxY, z).endVertex();
        buffer.pos(maxX, minY, z).endVertex();
    }

    static void drawPlanePosZ(final double z, final double minX, final double maxX, final double minY, final double maxY, final VertexBuffer buffer) {
        buffer.pos(minX, minY, z).endVertex();
        buffer.pos(maxX, minY, z).endVertex();
        buffer.pos(maxX, maxY, z).endVertex();
        buffer.pos(minX, maxY, z).endVertex();
    }

    static void drawArrow(final VertexBuffer buffer) {
        buffer.pos(0, 0, 0.5).endVertex();
        buffer.pos(0.5, 0, 1).endVertex();
        buffer.pos(1, 0, 0.5).endVertex();
        buffer.pos(0.25, 0, 0).endVertex();
        buffer.pos(0.25, 0, 0.5).endVertex();
        buffer.pos(0.75, 0, 0.5).endVertex();
        buffer.pos(0.25, 0, 0).endVertex();
        buffer.pos(0.75, 0, 0.5).endVertex();
        buffer.pos(0.75, 0, 0).endVertex();
    }

    private static void drawArrow3D(final VertexBuffer buffer) {
        final float trunkSize = 0.3f;
        final float trunkLength = 0.75f;
        final float tipLength = 0.75f;
        final float tipSize = 0.5f;

        // Tip
        buffer.pos(-tipSize, tipSize, 0).color(0.2f, 0.9f, 0.4f, 0.7f).endVertex();
        buffer.pos(0, 0, tipLength).color(0.2f, 0.9f, 0.4f, 0.7f).endVertex();
        buffer.pos(tipSize, tipSize, 0).color(0.2f, 0.9f, 0.4f, 0.7f).endVertex();

        buffer.pos(tipSize, -tipSize, 0).color(0.1f, 0.7f, 0.3f, 0.7f).endVertex();
        buffer.pos(0, 0, tipLength).color(0.1f, 0.7f, 0.3f, 0.7f).endVertex();
        buffer.pos(-tipSize, -tipSize, 0).color(0.1f, 0.7f, 0.3f, 0.7f).endVertex();

        buffer.pos(-tipSize, -tipSize, 0).color(0.2f, 0.85f, 0.4f, 0.7f).endVertex();
        buffer.pos(0, 0, tipLength).color(0.2f, 0.85f, 0.4f, 0.7f).endVertex();
        buffer.pos(-tipSize, tipSize, 0).color(0.2f, 0.85f, 0.4f, 0.7f).endVertex();

        buffer.pos(tipSize, tipSize, 0).color(0.2f, 0.85f, 0.4f, 0.7f).endVertex();
        buffer.pos(0, 0, tipLength).color(0.2f, 0.85f, 0.4f, 0.7f).endVertex();
        buffer.pos(tipSize, -tipSize, 0).color(0.2f, 0.85f, 0.4f, 0.7f).endVertex();

        // Base
        buffer.pos(trunkSize, trunkSize, -trunkLength).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();
        buffer.pos(-trunkSize, -trunkSize, -trunkLength).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();
        buffer.pos(-trunkSize, trunkSize, -trunkLength).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();
        buffer.pos(trunkSize, trunkSize, -trunkLength).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();
        buffer.pos(trunkSize, -trunkSize, -trunkLength).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();
        buffer.pos(-trunkSize, -trunkSize, -trunkLength).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();

        // Trunk
        buffer.pos(trunkSize, trunkSize, -trunkLength).color(0.2f, 0.9f, 0.4f, 0.7f).endVertex();
        buffer.pos(-trunkSize, trunkSize, 0).color(0.2f, 0.9f, 0.4f, 0.7f).endVertex();
        buffer.pos(trunkSize, trunkSize, 0).color(0.2f, 0.9f, 0.4f, 0.7f).endVertex();
        buffer.pos(trunkSize, trunkSize, -trunkLength).color(0.2f, 0.9f, 0.4f, 0.7f).endVertex();
        buffer.pos(-trunkSize, trunkSize, -trunkLength).color(0.2f, 0.9f, 0.4f, 0.7f).endVertex();
        buffer.pos(-trunkSize, trunkSize, 0).color(0.2f, 0.9f, 0.4f, 0.7f).endVertex();

        buffer.pos(-trunkSize, -trunkSize, -trunkLength).color(0.1f, 0.7f, 0.3f, 0.7f).endVertex();
        buffer.pos(trunkSize, -trunkSize, 0).color(0.1f, 0.7f, 0.3f, 0.7f).endVertex();
        buffer.pos(-trunkSize, -trunkSize, 0).color(0.1f, 0.7f, 0.3f, 0.7f).endVertex();
        buffer.pos(-trunkSize, -trunkSize, -trunkLength).color(0.1f, 0.7f, 0.3f, 0.7f).endVertex();
        buffer.pos(trunkSize, -trunkSize, -trunkLength).color(0.1f, 0.7f, 0.3f, 0.7f).endVertex();
        buffer.pos(trunkSize, -trunkSize, 0).color(0.1f, 0.7f, 0.3f, 0.7f).endVertex();

        buffer.pos(trunkSize, trunkSize, -trunkLength).color(0.15f, 0.75f, 0.35f, 0.7f).endVertex();
        buffer.pos(trunkSize, trunkSize, 0).color(0.15f, 0.75f, 0.35f, 0.7f).endVertex();
        buffer.pos(trunkSize, -trunkSize, 0).color(0.15f, 0.75f, 0.35f, 0.7f).endVertex();
        buffer.pos(trunkSize, trunkSize, -trunkLength).color(0.15f, 0.75f, 0.35f, 0.7f).endVertex();
        buffer.pos(trunkSize, -trunkSize, 0).color(0.15f, 0.75f, 0.35f, 0.7f).endVertex();
        buffer.pos(trunkSize, -trunkSize, -trunkLength).color(0.15f, 0.75f, 0.35f, 0.7f).endVertex();

        buffer.pos(-trunkSize, trunkSize, 0).color(0.15f, 0.75f, 0.35f, 0.7f).endVertex();
        buffer.pos(-trunkSize, trunkSize, -trunkLength).color(0.15f, 0.75f, 0.35f, 0.7f).endVertex();
        buffer.pos(-trunkSize, -trunkSize, -trunkLength).color(0.15f, 0.75f, 0.35f, 0.7f).endVertex();
        buffer.pos(-trunkSize, trunkSize, 0).color(0.15f, 0.75f, 0.35f, 0.7f).endVertex();
        buffer.pos(-trunkSize, -trunkSize, -trunkLength).color(0.15f, 0.75f, 0.35f, 0.7f).endVertex();
        buffer.pos(-trunkSize, -trunkSize, 0).color(0.15f, 0.75f, 0.35f, 0.7f).endVertex();

        // Tip base
        buffer.pos(tipSize, tipSize, 0).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();
        buffer.pos(-tipSize, trunkSize, 0).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();
        buffer.pos(-tipSize, tipSize, 0).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();
        buffer.pos(tipSize, tipSize, 0).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();
        buffer.pos(tipSize, trunkSize, 0).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();
        buffer.pos(-tipSize, trunkSize, 0).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();

        buffer.pos(tipSize, -trunkSize, 0).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();
        buffer.pos(-tipSize, -tipSize, 0).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();
        buffer.pos(-tipSize, -trunkSize, 0).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();
        buffer.pos(tipSize, -trunkSize, 0).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();
        buffer.pos(tipSize, -tipSize, 0).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();
        buffer.pos(-tipSize, -tipSize, 0).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();

        buffer.pos(-trunkSize, trunkSize, 0).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();
        buffer.pos(-trunkSize, -trunkSize, 0).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();
        buffer.pos(-tipSize, -trunkSize, 0).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();
        buffer.pos(-trunkSize, trunkSize, 0).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();
        buffer.pos(-tipSize, -trunkSize, 0).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();
        buffer.pos(-tipSize, trunkSize, 0).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();

        buffer.pos(tipSize, trunkSize, 0).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();
        buffer.pos(tipSize, -trunkSize, 0).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();
        buffer.pos(trunkSize, -trunkSize, 0).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();
        buffer.pos(tipSize, trunkSize, 0).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();
        buffer.pos(trunkSize, -trunkSize, 0).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();
        buffer.pos(trunkSize, trunkSize, 0).color(0.2f, 0.8f, 0.4f, 0.7f).endVertex();
    }

    private OverlayRendererUtils() {
    }
}
