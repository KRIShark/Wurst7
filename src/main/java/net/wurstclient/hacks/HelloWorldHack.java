package net.wurstclient.hacks;

import jdk.jfr.Description;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.wurstclient.Category;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;

@Description("The HelloWord Hack")
public final class HelloWorldHack extends Hack implements UpdateListener
{
    private boolean sentMsg = false;
    public HelloWorldHack()
    {
        super("HelloWorld");
        setCategory(Category.CHAT);
    }
    @Override
    public void onUpdate() {
        ClientPlayerEntity player = MC.player;
        if (!sentMsg){
            player.sendMessage(Text.literal("Hello World"), false);
            sentMsg = true;
        }
    }

    @Override
    public void onEnable()
    {
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    public void onDisable()
    {
        EVENTS.remove(UpdateListener.class, this);
        sentMsg = false;
    }
}
