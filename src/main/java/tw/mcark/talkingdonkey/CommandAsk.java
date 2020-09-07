package tw.mcark.talkingdonkey;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import tw.mcark.talkingdonkey.dialogflow.DetectIntentTexts;

public class CommandAsk extends Command {

    public CommandAsk() {
        super("ask");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (args.length == 0) {
            if (sender instanceof ProxiedPlayer) {
                ProxiedPlayer player = (ProxiedPlayer) sender;
                IntentListener.addAskedPlayer(player.getUniqueId());
                player.sendMessage(new ComponentBuilder("請輸入問題：").color(ChatColor.GREEN).create());
            }
            return;
        }

        StringBuilder queryText = new StringBuilder();

        for (String arg : args) {
            queryText.append(arg).append(" ");
        }

        DetectIntentTexts.ask(sender, queryText.toString());
    }


}
