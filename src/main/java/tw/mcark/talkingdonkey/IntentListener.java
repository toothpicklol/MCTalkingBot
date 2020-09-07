package tw.mcark.talkingdonkey;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import tw.mcark.talkingdonkey.dialogflow.DetectIntentTexts;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class IntentListener implements Listener {

    private static HashSet<UUID> askedPlayer = new HashSet<>();
    private static HashMap<UUID, UUID> queueQuestion = new HashMap<>();

    public static void addAskedPlayer(UUID player) {
        askedPlayer.add(player);
    }

    public static void addQueueQuestion(UUID player, UUID sessionId) {
        queueQuestion.put(player, sessionId);
    }

    @EventHandler
    public void onEvent(ChatEvent event) {
        if (!event.isCommand() && event.getSender() instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) event.getSender();
            if (askedPlayer.contains(player.getUniqueId())) {
                event.setCancelled(true);
                DetectIntentTexts.ask(player, event.getMessage());
                askedPlayer.remove(player.getUniqueId());
            }
            if (queueQuestion.containsKey(player.getUniqueId())) {
                event.setCancelled(true);
                if (DetectIntentTexts.answer(queueQuestion.get(player.getUniqueId()), event.getMessage())) {
                    player.sendMessage(new ComponentBuilder("成功回答問題!").color(ChatColor.GREEN).create());
                } else {
                    player.sendMessage(new ComponentBuilder("問題已逾時或已被回答了!").color(ChatColor.RED).create());
                }
                queueQuestion.remove(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onEvent(PluginMessageEvent event) {
        if (event.getTag().equalsIgnoreCase("salvage:talking_donkey")) {
            try (ByteArrayInputStream stream = new ByteArrayInputStream(event.getData());
                 ObjectInputStream in = new ObjectInputStream(stream)) {
                UUID uuid = UUID.fromString(in.readUTF());
                ProxiedPlayer player = TalkingDonkey.getInstance().getProxy().getPlayer(uuid);
                if (player!=null && player.isConnected()) {
                    IntentListener.addAskedPlayer(player.getUniqueId());
                    player.sendMessage(new ComponentBuilder("請輸入問題：").color(ChatColor.GREEN).create());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
