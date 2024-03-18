package net.wurstclient.hacks;

import jdk.jfr.Description;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.wurstclient.Category;
import net.wurstclient.WurstClient;
import net.wurstclient.events.ChatOutputListener;
import net.wurstclient.events.DeathListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.TextFieldSetting;

@Description("The Last words on Death Hack")
public final class LastWordsHack extends Hack implements DeathListener, ChatOutputListener
{
    private boolean sentMsg = false;
    private final TextFieldSetting msg = new TextFieldSetting("Last Words","Auch I am dead.");
    public LastWordsHack()
    {
        super("LastWords");
        setCategory(Category.CHAT);
        addSetting(msg);
    }

    @Override
    public void onEnable()
    {
        EVENTS.add(ChatOutputListener.class, this);
        EVENTS.add(DeathListener.class, this);
    }

    @Override
    public void onDisable()
    {
        EVENTS.remove(ChatOutputListener.class, this);
        EVENTS.remove(DeathListener.class, this);
        sentMsg = false;
    }

    @Override
    public void onDeath() {
        if(!sentMsg && WurstClient.INSTANCE.isEnabled()){
            sentMsg = true;
            ClientPlayerEntity player = MC.player;
            var message = msg.getValue();
            player.sendMessage(Text.literal(message), false);
        }

    }

    @Override
    public void onSentMessage(ChatOutputEvent event) {
        String message = event.getOriginalMessage().trim();
        if (message == msg.getValue()){
            sentMsg = false;
        }
    }
}
