package li.cil.architect.common.common;

import li.cil.architect.api.API;
import li.cil.architect.common.config.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandArchitect extends CommandBase {
    private final Map<String, CommandBase> subCommands = new HashMap<>();

    public CommandArchitect() {
        addSubCommand(new Blacklist());
        addSubCommand(new Whitelist());
        addSubCommand(new AttachedBlockList());
        addSubCommand(new BlockMapping());
        addSubCommand(new ItemMapping());
    }

    private void addSubCommand(final CommandBase command) {
        subCommands.put(command.getName(), command);
    }

    // --------------------------------------------------------------------- //
    // Command

    @Override
    public String getName() {
        return API.MOD_ID;
    }

    @Override
    public String getUsage(final ICommandSender sender) {
        return "commands.architect.usage";
    }

    @Override
    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException(getUsage(sender));
        }

        final CommandBase subCommand = subCommands.get(args[0]);
        if (subCommand == null) {
            throw new WrongUsageException(getUsage(sender));
        }

        subCommand.execute(server, sender, getSubArgs(args));
    }

    @Override
    public List<String> getTabCompletions(final MinecraftServer server, final ICommandSender sender, final String[] args, @Nullable final BlockPos targetPos) {
        if (args.length > 1) {
            final CommandBase subCommand = subCommands.get(args[0]);
            if (subCommand == null) {
                return Collections.emptyList();
            }

            return subCommand.getTabCompletions(server, sender, getSubArgs(args), targetPos);
        }

        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, subCommands.keySet());
        }

        return Collections.emptyList();
    }

    // --------------------------------------------------------------------- //

    private String[] getSubArgs(final String[] args) {
        final String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, subArgs.length);
        return subArgs;
    }

    @Nullable
    private static ResourceLocation getLookedAtResourceLocation(final ICommandSender sender) {
        final IBlockState state = getLookedAtBlockState(sender);
        if (state == null) {
            return null;
        }

        return state.getBlock().getRegistryName();
    }

    @Nullable
    private static IBlockState getLookedAtBlockState(final ICommandSender sender) {
        final BlockPos pos = getLookedAtBlockPos(sender);
        if (pos == null) {
            return null;
        }

        return sender.getEntityWorld().getBlockState(pos);
    }

    @Nullable
    private static BlockPos getLookedAtBlockPos(final ICommandSender sender) {
        final Entity entity = sender.getCommandSenderEntity();
        if (!(entity instanceof EntityPlayer)) {
            return null;
        }

        final EntityPlayer player = (EntityPlayer) entity;
        final World world = player.getEntityWorld();
        final Vec3d origin = player.getPositionEyes(1);
        final Vec3d lookVec = player.getLookVec();
        final float blockReachDistance = player.isCreative() ? 5 : 4.5f;
        final Vec3d lookAt = origin.add(lookVec.scale(blockReachDistance));
        final RayTraceResult hit = world.rayTraceBlocks(origin, lookAt);
        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            return null;
        }

        final BlockPos hitPos = hit.getBlockPos();
        if (!world.isBlockLoaded(hitPos)) {
            return null;
        }

        return hitPos;
    }

    // --------------------------------------------------------------------- //

    private static final class Blacklist extends AbstractListCommand {
        @Override
        public String getName() {
            return "blacklist";
        }

        @Override
        public List<String> getAliases() {
            return Collections.singletonList("bl");
        }

        @Override
        protected boolean addToList(final ResourceLocation location) {
            return Settings.addToBlacklist(location);
        }

        @Override
        protected boolean removeFromList(final ResourceLocation location) {
            return Settings.removeFromBlacklist(location);
        }
    }

    private static final class Whitelist extends AbstractListCommand {
        @Override
        public String getName() {
            return "whitelist";
        }

        @Override
        public List<String> getAliases() {
            return Collections.singletonList("wl");
        }

        @Override
        protected boolean addToList(final ResourceLocation location) {
            return Settings.addToWhitelist(location);
        }

        @Override
        protected boolean removeFromList(final ResourceLocation location) {
            return Settings.removeFromWhitelist(location);
        }
    }

    private static final class AttachedBlockList extends AbstractListCommand {
        @Override
        public String getName() {
            return "attached";
        }

        @Override
        protected boolean addToList(final ResourceLocation location) {
            return Settings.addToAttachedBlockList(location);
        }

        @Override
        protected boolean removeFromList(final ResourceLocation location) {
            return Settings.removeFromAttachedBlockList(location);
        }
    }

    private static final class BlockMapping extends AbstractMappingCommand {
        @Override
        public String getName() {
            return "mapToBlock";
        }

        @Override
        public List<String> getAliases() {
            return Collections.singletonList("m2b");
        }

        @Nullable
        @Override
        protected ResourceLocation getMapping(final ResourceLocation location) {
            return Settings.mapBlockToBlock(location);
        }

        @Override
        protected boolean addMapping(final ResourceLocation location, final ResourceLocation mapping) {
            return Settings.addBlockMapping(location, mapping);
        }

        @Override
        protected boolean removeMapping(final ResourceLocation location) {
            return Settings.removeBlockMapping(location);
        }

        @Override
        protected Collection<ResourceLocation> getCandidates() {
            return Block.REGISTRY.getKeys();
        }
    }

    private static final class ItemMapping extends AbstractMappingCommand {
        @Override
        public String getName() {
            return "mapToItem";
        }

        @Override
        public List<String> getAliases() {
            return Collections.singletonList("m2i");
        }

        @Nullable
        @Override
        protected ResourceLocation getMapping(final ResourceLocation location) {
            return Settings.mapBlockToItem(location);
        }

        @Override
        protected boolean addMapping(final ResourceLocation location, final ResourceLocation mapping) {
            return Settings.addItemMapping(location, mapping);
        }

        @Override
        protected boolean removeMapping(final ResourceLocation location) {
            return Settings.removeItemMapping(location);
        }

        @Override
        protected Collection<ResourceLocation> getCandidates() {
            return Item.REGISTRY.getKeys();
        }
    }

    private static abstract class AbstractListCommand extends CommandBase {
        @Override
        public String getUsage(final ICommandSender sender) {
            return "commands.architect." + getName() + ".usage";
        }

        @Override
        public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
            final ResourceLocation location = getLookedAtResourceLocation(sender);
            if (location == null) {
                return;
            }

            if (args.length < 1) {
                if (addToList(location)) {
                    notifyCommandListener(sender, this, "commands.architect." + getName() + ".added", location);
                } else {
                    removeFromList(location);
                    notifyCommandListener(sender, this, "commands.architect." + getName() + ".removed", location);
                }
            } else if (args.length == 1) {
                if ("add".equals(args[0])) {
                    if (addToList(location)) {
                        notifyCommandListener(sender, this, "commands.architect." + getName() + ".added", location);
                    }
                } else if ("remove".equals(args[0])) {
                    if (removeFromList(location)) {
                        notifyCommandListener(sender, this, "commands.architect." + getName() + ".removed", location);
                    }
                } else {
                    throw new WrongUsageException(getUsage(sender));
                }
            } else {
                throw new WrongUsageException(getUsage(sender));
            }
        }

        @Override
        public List<String> getTabCompletions(final MinecraftServer server, final ICommandSender sender, final String[] args, @Nullable final BlockPos targetPos) {
            if (args.length == 1) {
                return getListOfStringsMatchingLastWord(args, Arrays.asList("add", "remove"));
            }

            return Collections.emptyList();
        }

        // --------------------------------------------------------------------- //

        protected abstract boolean addToList(final ResourceLocation location);

        protected abstract boolean removeFromList(final ResourceLocation location);
    }

    private static abstract class AbstractMappingCommand extends CommandBase {
        @Override
        public String getUsage(final ICommandSender sender) {
            return "commands.architect." + getName() + ".usage";
        }

        @Override
        public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
            final ResourceLocation location = getLookedAtResourceLocation(sender);
            if (location == null) {
                return;
            }

            if (args.length < 1) {
                final ResourceLocation mapping = getMapping(location);
                if (mapping != null) {
                    notifyCommandListener(sender, this, "commands.architect." + getName() + ".mapping", location, mapping);
                } else {
                    notifyCommandListener(sender, this, "commands.architect." + getName() + ".nomapping", location);
                }
            } else if (args.length == 1) {
                if ("clear".equals(args[0])) {
                    if (removeMapping(location)) {
                        notifyCommandListener(sender, this, "commands.architect." + getName() + ".removed", location);
                    } else {
                        notifyCommandListener(sender, this, "commands.architect." + getName() + ".nomapping", location);
                    }
                } else {
                    final ResourceLocation mapping = new ResourceLocation(args[0]);
                    if (addMapping(location, mapping)) {
                        notifyCommandListener(sender, this, "commands.architect." + getName() + ".added", location, mapping);
                    } else {
                        throw new WrongUsageException(getUsage(sender));
                    }
                }
            } else {
                throw new WrongUsageException(getUsage(sender));
            }
        }

        @Override
        public List<String> getTabCompletions(final MinecraftServer server, final ICommandSender sender, final String[] args, @Nullable final BlockPos targetPos) {
            if (args.length == 1) {
                final List<Object> candidates = new ArrayList<>();
                candidates.add("clear");
                candidates.addAll(getCandidates());
                return getListOfStringsMatchingLastWord(args, candidates);
            }

            return Collections.emptyList();
        }

        // --------------------------------------------------------------------- //

        @Nullable
        protected abstract ResourceLocation getMapping(final ResourceLocation location);

        protected abstract boolean addMapping(final ResourceLocation location, final ResourceLocation mapping);

        protected abstract boolean removeMapping(final ResourceLocation location);

        protected abstract Collection<ResourceLocation> getCandidates();
    }
}
