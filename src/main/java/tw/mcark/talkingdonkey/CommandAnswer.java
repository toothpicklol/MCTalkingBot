package tw.mcark.talkingdonkey;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import tw.mcark.talkingdonkey.dialogflow.DetectIntentTexts;

import java.util.UUID;

public class CommandAnswer extends Command {

    public CommandAnswer() {
        super("answer");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length<1) {
            return;
        }
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            UUID sessionId = UUID.fromString(args[0]);

            if (DetectIntentTexts.isValidSession(sessionId)) {
                IntentListener.addQueueQuestion(player.getUniqueId(), sessionId);
                player.sendMessage(new ComponentBuilder("請輸入答案：").color(ChatColor.GREEN).create());
            } else {
                player.sendMessage(new ComponentBuilder("無效的問題或已逾時或已被回答了!").color(ChatColor.RED).create());
            }



        }
    }
}
