package me.freezy.plugins.papermc.blazesmp.command;

import me.freezy.plugins.papermc.blazesmp.BlazeSMP;
import me.freezy.plugins.papermc.blazesmp.command.util.SimpleCommand;
import me.freezy.plugins.papermc.blazesmp.module.manager.L4M4;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReportCommand extends SimpleCommand {
    MiniMessage miniMessage = MiniMessage.miniMessage();

    public ReportCommand() {
        super("report");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player reporter)) {
            sender.sendMessage(miniMessage.deserialize(L4M4.get("report.error.not_a_player")));
            return true;
        }

        if (args.length < 2) {
            reporter.sendMessage(miniMessage.deserialize(L4M4.get("report.usage")));
            return true;
        }

        OfflinePlayer reportedPlayer = Bukkit.getOfflinePlayer(args[0]);
        if (!reportedPlayer.hasPlayedBefore() && !reportedPlayer.isOnline()) {
            reporter.sendMessage(miniMessage.deserialize(L4M4.get("report.error.invalid_player")));
            return true;
        }

        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        reporter.sendMessage(miniMessage.deserialize(String.format(L4M4.get("report.success.reported"),
                reportedPlayer.getName(), reason)));

        sendReportToDiscord(reporter.getName(), reportedPlayer.getName(), reason, reporter.getUniqueId());

        return true;
    }

    private void sendReportToDiscord(String reporter, String reported, String reason, UUID reporterUUID) {
        try {
            String thumbnailUrl = L4M4.get("report.discord.thumbnail_url_base");
            String jsonPayload = "{"
                    + "\"username\": \"" + L4M4.get("report.discord.username") + "\","
                    + "\"embeds\": [{"
                    + "\"title\": \"" + L4M4.get("report.discord.title") + "\","
                    + "\"color\": 16711680,"
                    + "\"thumbnail\": {\"url\": \"" + thumbnailUrl + reporterUUID.toString() + "\"},"
                    + "\"fields\": ["
                    + "{\"name\": \"" + L4M4.get("report.discord.field.reporter") + "\", \"value\": \"" + reporter + "\", \"inline\": true},"
                    + "{\"name\": \"" + L4M4.get("report.discord.field.reported") + "\", \"value\": \"" + reported + "\", \"inline\": true},"
                    + "{\"name\": \"" + L4M4.get("report.discord.field.reason") + "\", \"value\": \"" + reason + "\", \"inline\": false}"
                    + "]"
                    + "}]"
                    + "}";
            HttpURLConnection connection = getHttpURLConnection(jsonPayload);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_NO_CONTENT || responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Report sent to Discord successfully!");
            } else {
                System.out.println("Failed to send report to Discord. Response code: " + responseCode);
            }
            connection.disconnect();
        } catch (Exception e) {
            BlazeSMP.getInstance().getLog().error("Failed to send report to Discord: {}", e.getMessage());
        }
    }

    private static @NotNull HttpURLConnection getHttpURLConnection(String jsonPayload) throws IOException {
        String webhookUrl = BlazeSMP.getInstance().getConfig().getString("discord-report-webhook");
        if (webhookUrl == null) {
            throw new IOException("Discord report webhook URL not set in config!");
        }
        URL url = new URL(webhookUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        return connection;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.startsWith(args[0]))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
