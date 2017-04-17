package li.cil.architect.common.command;

import li.cil.architect.api.converter.SortIndex;
import li.cil.architect.common.config.Constants;
import li.cil.architect.common.config.Jasons;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class SubCommandWhitelist extends AbstractListCommand {
    private static final String SORT_INDEX_ATTACHED = "attached";
    private static final String SORT_INDEX_FALLING = "falling";
    private static final String SORT_INDEX_FLUID = "fluid";

    @Override
    public String getName() {
        return "whitelist";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("wl");
    }

    @Override
    public List<String> getTabCompletions(final MinecraftServer server, final ICommandSender sender, final String[] args, @Nullable final BlockPos targetPos) {
        if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, Arrays.asList(SORT_INDEX_FALLING, SORT_INDEX_ATTACHED, SORT_INDEX_FLUID));
        }

        return super.getTabCompletions(server, sender, args, targetPos);
    }

    @Override
    protected boolean addToList(final ICommandSender sender, final String[] args, final IBlockState state, final ResourceLocation location) throws CommandException {
        final int sortIndex;
        if (args.length == 0) {
            sortIndex = SortIndex.SOLID_BLOCK;
        } else if (SORT_INDEX_ATTACHED.equals(args[0])) {
            sortIndex = SortIndex.ATTACHED_BLOCK;
        } else if (SORT_INDEX_FALLING.equals(args[0])) {
            sortIndex = SortIndex.FALLING_BLOCK;
        } else if (SORT_INDEX_FLUID.equals(args[0])) {
            sortIndex = SortIndex.FLUID_BLOCK;
        } else {
            sortIndex = parseInt(args[0]);
        }

        // Grab nbt from tile entity.
        final NBTTagCompound nbt = getLookedAtTileEntityNBT(sender);
        if (nbt == null) {
            notifyCommandListener(sender, this, Constants.COMMAND_NBT_NO_TILE_ENTITY);
            return false;
        }

        return Jasons.addToWhitelist(state.getBlock(), state.getProperties(), sortIndex, convert(nbt), Collections.emptyMap());
    }

    @Override
    protected boolean removeFromList(final ResourceLocation location) {
        return Jasons.removeFromWhitelist(location);
    }

    private static Map<String, Object> convert(final NBTTagCompound nbt) {
        final Map<String, Object> result = new HashMap<>();
        for (final String key : nbt.getKeySet()) {
            final NBTBase value = nbt.getTag(key);
            if (value instanceof NBTTagCompound) {
                result.put(key, convert((NBTTagCompound) value));
            } else {
                result.put(key, NBTBase.NBT_TYPES[value.getId()]);
            }
        }
        return result;
    }
}
