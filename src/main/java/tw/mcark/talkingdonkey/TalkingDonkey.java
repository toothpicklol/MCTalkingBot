package tw.mcark.talkingdonkey;

import net.md_5.bungee.api.plugin.Plugin;
import tw.mcark.talkingdonkey.dialogflow.DetectIntentTexts;

import java.io.IOException;
import java.util.Collections;

public class TalkingDonkey extends Plugin {

    private static TalkingDonkey instance;

    public static TalkingDonkey getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        getProxy().registerChannel("salvage:talking_donkey");
        getProxy().getPluginManager().registerCommand(this, new CommandAsk());
        getProxy().getPluginManager().registerCommand(this, new CommandAnswer());
        getProxy().getPluginManager().registerListener(this, new IntentListener());

    }
}
