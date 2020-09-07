package tw.mcark.talkingdonkey.dialogflow;

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.TextInput;
import com.google.common.collect.Maps;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import tw.mcark.talkingdonkey.TalkingDonkey;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DetectIntentTexts {

    private static HashMap<UUID, String> unansweredIntent = new HashMap<>();
    private static HashMap<UUID, UUID> unansweredPlayer = new HashMap<>();

    static {
        TalkingDonkey talkingDonkey = TalkingDonkey.getInstance();
        talkingDonkey.getProxy().getScheduler().schedule(talkingDonkey, () -> {
            unansweredIntent.entrySet().removeIf(entry -> {
                if (!entry.getValue().isBlank()) {
                    ProxiedPlayer player = talkingDonkey.getProxy().getPlayer(unansweredPlayer.get(entry.getKey()));
                    if (player != null && player.isConnected()) {
                        player.sendMessage(new ComponentBuilder("導師大發慈悲的告訴你: ").color(ChatColor.GREEN).append(entry.getValue()).color(ChatColor.WHITE).create());
                    }
                    unansweredPlayer.remove(entry.getKey());
                    return true;
                }
                return false;
            });
        }, 1, 1, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws IOException {

    }

    public static boolean isValidSession(UUID sessionId) {
        return unansweredIntent.containsKey(sessionId);
    }

    public static boolean answer(UUID sessionId, String answer) {
        if (unansweredIntent.containsKey(sessionId)) {
            unansweredIntent.put(sessionId, answer);
            return true;
        }
        return false;
    }

    public static void ask(CommandSender sender, String question) {
        sender.sendMessage(new ComponentBuilder("您誠心誠意的提問了: ").color(ChatColor.GREEN).append(question).color(ChatColor.WHITE).create());
        TalkingDonkey talkingDonkey = TalkingDonkey.getInstance();
        int delay = (int) (Math.random() * 1250);
        delay += 250;
        UUID sessionId = UUID.randomUUID();
        talkingDonkey.getProxy().getScheduler().schedule(talkingDonkey, () -> {
            talkingDonkey.getProxy().getScheduler().runAsync(talkingDonkey, () -> {
                try {
                    Map<String, QueryResult> resultMap = DetectIntentTexts.detectIntentTexts("mcark-227611", Collections.singletonList(question), sessionId.toString(), "zhTW");
                    QueryResult queryResult = resultMap.get(question);
                    if (queryResult.getIntent().getIsFallback()) {
                        if (sender instanceof ProxiedPlayer) {
                            ProxiedPlayer player = (ProxiedPlayer) sender;
                            unansweredIntent.put(sessionId, "");
                            unansweredPlayer.put(sessionId, player.getUniqueId());
                            questionForward(sessionId, question);
                            TalkingDonkey.getInstance().getProxy().getScheduler().schedule(talkingDonkey, () -> {
                                if (unansweredIntent.containsKey(sessionId) && unansweredIntent.get(sessionId).isBlank()) {
                                    sender.sendMessage(new ComponentBuilder("導師大發慈悲的告訴你: ").color(ChatColor.GREEN).append(resultMap.get(question).getFulfillmentText()).color(ChatColor.WHITE).create());
                                    unansweredIntent.remove(sessionId);
                                    unansweredPlayer.remove(sessionId);
                                }
                            }, 60, TimeUnit.SECONDS);
                        }
                    } else {
                        sender.sendMessage(new ComponentBuilder("導師大發慈悲的告訴你: ").color(ChatColor.GREEN).append(resultMap.get(question).getFulfillmentText()).color(ChatColor.WHITE).create());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }, delay, TimeUnit.MILLISECONDS);
    }

    public static void questionForward(UUID sessionId, String question) {
        for (ProxiedPlayer player : TalkingDonkey.getInstance().getProxy().getPlayers()) {
            if (player.hasPermission("group.magistrate")) {
                player.sendMessage(new ComponentBuilder("玩家問了導師一個無法回答的問題: ").color(ChatColor.GREEN).append(question).color(ChatColor.WHITE).append("[回答]").color(ChatColor.YELLOW).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/answer " + sessionId.toString())).create());
            }
        }

    }

    public static Map<String, QueryResult> detectIntentTexts(String projectId, List<String> texts, String sessionId, String languageCode) throws IOException, ApiException {
        Map<String, QueryResult> queryResults = Maps.newHashMap();
        try (SessionsClient sessionsClient = SessionsClient.create()) {
            SessionName session = SessionName.of(projectId, sessionId);
            for (String text : texts) {
                TextInput.Builder textInput = TextInput.newBuilder().setText(text).setLanguageCode(languageCode);
                QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();
                DetectIntentResponse response = sessionsClient.detectIntent(session, queryInput);
                QueryResult queryResult = response.getQueryResult();
                queryResults.put(text, queryResult);
            }
        }
        return queryResults;
    }
}
