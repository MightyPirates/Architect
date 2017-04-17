package li.cil.architect.common.command;

import li.cil.architect.common.config.Jasons;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

final class SubCommandBlacklist extends AbstractListCommand {
    @Override
    public String getName() {
        return "blacklist";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("bl");
    }

    @Override
    public List<String> getTabCompletions(final MinecraftServer server, final ICommandSender sender, final String[] args, @Nullable final BlockPos targetPos) {
        if (args.length > 1) {
            try {
                final IBlockState state = getLookedAtBlockState(sender);
                if (state != null) {
                    return getListOfStringsMatchingLastWord(args, state.getPropertyKeys().stream().map(IProperty::getName).filter(n -> !containsProperty(args, n)).collect(Collectors.toList()));
                }
            } catch (final CommandException ignored) {
            }
        }

        return super.getTabCompletions(server, sender, args, targetPos);
    }

    @Override
    protected boolean addToList(final ICommandSender sender, final String[] args, final IBlockState state, final ResourceLocation location) {
        final Map<IProperty<?>, Comparable<?>> constraintList = new HashMap<>();
        if (args.length > 0) {
            for (final Map.Entry<IProperty<?>, Comparable<?>> property : state.getProperties().entrySet()) {
                if (ArrayUtils.contains(args, property.getKey().getName())) {
                    constraintList.put(property.getKey(), property.getValue());
                }
            }
        }
        return Jasons.addToBlacklist(state.getBlock(), constraintList);
    }

    @Override
    protected boolean removeFromList(final ResourceLocation location) {
        return Jasons.removeFromBlacklist(location);
    }

    private static boolean containsProperty(final String[] args, final String value) {
        // Skip first, it's the command itself.
        for (int i = 1; i < args.length; i++) {
            if (Objects.equals(args[i], value)) {
                return true;
            }
        }
        return false;
    }
}
