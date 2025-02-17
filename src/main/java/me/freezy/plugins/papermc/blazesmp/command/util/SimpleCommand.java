package me.freezy.plugins.papermc.blazesmp.command.util;

import me.freezy.plugins.papermc.blazesmp.BlazeSMP;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public abstract class SimpleCommand implements CommandExecutor, TabExecutor {
    protected static Logger logger = Logger.getLogger(BlazeSMP.class.getName());
    protected static CommandMap cmap;
    protected final String command;
    protected final String description;
    protected final List<String> alias;
    protected final String usage;
    protected final String permission;

    public SimpleCommand(String command) {
        this(command, null, null, null, null);
    }

    public SimpleCommand(String command, String usage) {
        this(command, usage, null, null, null);
    }

    public SimpleCommand(String command, String usage, String description) {
        this(command, usage, description, null, null);
    }

    public SimpleCommand(String command, String usage, String description, List<String> alias) {
        this(command, usage, description, null, alias);
    }

    public SimpleCommand(String command, String usage, String description, String permission) {
        this(command, usage, description, permission, null);
    }

    public SimpleCommand(String command, String usage, String description, String permission, List<String> alias) {
        this.command = command;
        this.description = description;
        this.alias = alias;
        this.usage = usage;
        this.permission = permission;
    }

    public void register() {
        ReflectCommand cmd = new ReflectCommand(this.command);
        if (this.alias != null) cmd.setAliases(this.alias);
        if (this.description != null) cmd.setDescription(this.description);
        if (this.usage != null) cmd.setUsage(this.usage);
        if (this.permission != null) cmd.setPermission(this.permission);
        getCommandMap().register("blazesmp", cmd);
        cmd.setExecutor(this);
    }

    final CommandMap getCommandMap() {
        if (cmap == null) {
            try {
                final Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                f.setAccessible(true);
                cmap = (CommandMap) f.get(Bukkit.getServer());
                return getCommandMap();
            } catch (Exception e) {
                logger.severe(String.valueOf(e));
            }
        } else {
            return cmap;
        }
        return getCommandMap();
    }

    private static final class ReflectCommand extends Command {
        private SimpleCommand exe = null;

        private ReflectCommand(String command) {
            super(command);
        }

        public void setExecutor(SimpleCommand exe) {
            this.exe = exe;
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
            if (exe != null) {
                return exe.onCommand(sender, this, label, args);
            }
            return false;
        }

        @Override
        public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args) {
            if (exe != null) {
                return Objects.requireNonNull(exe.onTabComplete(sender, this, alias, args));
            }
            return List.of();
        }
    }
}
