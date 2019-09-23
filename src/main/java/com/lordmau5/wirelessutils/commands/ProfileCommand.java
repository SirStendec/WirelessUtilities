package com.lordmau5.wirelessutils.commands;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.utils.mod.ModPermissions;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ProfileCommand extends CommandBase {

    private static final Logger LOGGER = LogManager.getLogger();

    public static boolean wantsToStart = false;

    private long profileStartTime;
    private int profileStartTick;

    public String getName() {
        return "wu_profile";
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return ModPermissions.COMMAND_PROFILE.hasPermission(sender);
    }

    public String getUsage(ICommandSender sender) {
        return "commands." + WirelessUtils.MODID + ".profile.usage";
    }

    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if ( args.length != 1 )
            throw new WrongUsageException("commands." + WirelessUtils.MODID + ".profile.usage");

        if ( "start".equalsIgnoreCase(args[0]) ) {
            if ( WirelessUtils.profiler.profilingEnabled )
                throw new CommandException("commands." + WirelessUtils.MODID + ".profile.already_started");

            notifyCommandListener(sender, this, "commands." + WirelessUtils.MODID + ".profile.start");
            profileStartTime = MinecraftServer.getCurrentTimeMillis();
            profileStartTick = server.getTickCounter();
            wantsToStart = true;
            return;
        }

        if ( "stop".equalsIgnoreCase(args[0]) ) {
            if ( !WirelessUtils.profiler.profilingEnabled )
                throw new CommandException("commands." + WirelessUtils.MODID + ".profile.not_started");

            long now = MinecraftServer.getCurrentTimeMillis();
            int tick = server.getTickCounter();

            long duration = now - profileStartTime;
            int ticks = tick - profileStartTick;

            saveProfilerResults(duration, ticks, server);
            WirelessUtils.profiler.profilingEnabled = false;

            notifyCommandListener(sender, this, "commands." + WirelessUtils.MODID + ".profile.stop", String.format("%.2f", (float) duration / 1000), ticks);
            return;
        }

        throw new WrongUsageException("commands." + WirelessUtils.MODID + ".profile.usage");
    }

    private void saveProfilerResults(long duration, int ticks, MinecraftServer server) {
        File file = new File(server.getFile("debug"), "wirelessutils-profile-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + ".txt");
        file.getParentFile().mkdirs();
        Writer writer = null;

        try {
            writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            writer.write(getProfilerResults(duration, ticks, server));
        } catch (Throwable ex) {
            LOGGER.error("Could not save profiling results to {}", file, ex);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    private String getProfilerResults(long duration, int ticks, MinecraftServer server) {
        StringBuilder builder = new StringBuilder();
        builder.append("---- Wireless Utilities Profiler Results ----\n\n");
        builder.append("Duration: ").append(duration).append(" ms\n");
        builder.append("Ticks: ").append(ticks).append(" ticks\n");
        builder.append("Average Tick Rate: ").append((float) ticks / ((float) duration / 1000F)).append(" ticks per second\n");
        builder.append("--- BEGIN PROFILE DUMP ---\n\n");

        appendResults(0, "root", builder);

        builder.append("\n--- END PROFILE DUMP ---\n");
        return builder.toString();
    }

    private void appendResults(int depth, String section, StringBuilder builder) {
        List<Profiler.Result> results = WirelessUtils.profiler.getProfilingData(section);
        if ( results == null || results.size() < 3 )
            return;

        for (int i = 1; i < results.size(); ++i) {
            Profiler.Result result = results.get(i);
            if ( result == null )
                continue;

            builder.append(String.format("[%02d] ", depth));

            for (int j = 0; j < depth; j++)
                builder.append("|   ");

            builder.append(result.profilerName).append(" - ").append(String.format("%.2f", result.usePercentage)).append("%/").append(String.format("%.2f", result.totalUsePercentage)).append("%\n");

            if ( !"unspecified".equals(result.profilerName) )
                try {
                    appendResults(depth + 1, section + "." + result.profilerName, builder);
                } catch (Exception ex) {
                    builder.append("[[ EXCEPTION ").append(ex).append(" ]]");
                }
        }
    }

    @Override
    @Nonnull
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, "start", "stop") : Collections.emptyList();
    }
}
