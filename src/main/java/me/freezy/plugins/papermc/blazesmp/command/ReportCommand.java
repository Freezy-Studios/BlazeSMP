package me.freezy.plugins.papermc.blazesmp.command;

import me.freezy.plugins.papermc.blazesmp.BlazeSMP;
import me.freezy.plugins.papermc.blazesmp.command.util.SimpleCommand;
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
            sender.sendMessage(miniMessage.deserialize("<b><i><color:#ff002f>Du darfst das nicht</color></i></b>"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(miniMessage.deserialize("<i><color:#ff0048>Benutze bitte:</color> <color:#ff00ee>/report</color> <blue><Spieler></blue> <dark_green><Grund></dark_green></i>"));
            return true;
        }

        OfflinePlayer reportedPlayer = Bukkit.getOfflinePlayer(args[0]);
        if (!reportedPlayer.hasPlayedBefore() && !reportedPlayer.isOnline()) {
            reporter.sendMessage(miniMessage.deserialize("<color:#ff002f>Der Spieler existiert nicht oder war noch nie online!</color>"));
            return true;
        }

        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        reporter.sendMessage(miniMessage.deserialize(String.format("<green>Du hast</green> <white><b>%s</b></white> <green>erfolgreich wegen</green> <light_purple><i>%s</i></light_purple> <green>gemeldet</green>", reportedPlayer.getName(), reason)));

        sendReportToDiscord(reporter.getName(), reportedPlayer.getName(), reason, reporter.getUniqueId());

        return true;
    }

    private void sendReportToDiscord(String reporter, String reported, String reason, UUID reporterUUID) {
        try {
            String thumbnailUrl = "http://209.25.141.65:40018/v1/head/getHead/";
            String jsonPayload = "{"
                    + "\"username\": \"ReportBot\","
                    + "\"embeds\": [{"
                    + "\"title\": \"Neuer Report\","
                    + "\"color\": 16711680," // Red color
                    + "\"thumbnail\": {\"url\": \"" + thumbnailUrl+reporterUUID.toString() + "\"}," // Thumbnail
                    + "\"fields\": ["
                    + "{\"name\": \"Reporter\", \"value\": \"" + reporter + "\", \"inline\": true},"
                    + "{\"name\": \"Gemeldeter Spieler\", \"value\": \"" + reported + "\", \"inline\": true},"
                    + "{\"name\": \"Grund\", \"value\": \"" + reason + "\", \"inline\": false}"
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
            e.printStackTrace();
        }
    }

    private static @NotNull HttpURLConnection getHttpURLConnection(String jsonPayload) throws IOException {
        String webhookUrl = BlazeSMP.getInstance().getConfig().getString("discord-report-webhook");
        assert webhookUrl != null;
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