package com.mykingdom.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class KingdomScreen extends Screen {
    public KingdomScreen() { super(Text.literal("My Kingdom")); }
    @Override protected void init() {
        int cx=width/2, y=height/2-55;
        addDrawableChild(ButtonWidget.builder(Text.literal("Огляд королівства"), b -> command("kingdom")).dimensions(cx-100,y,200,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Населення та професії"), b -> command("kingdom citizens")).dimensions(cx-100,y+25,200,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Побудувати будинок (35 монет)"), b -> command("kingdom build house")).dimensions(cx-100,y+50,200,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Побудувати ферму (20 монет)"), b -> command("kingdom build farm")).dimensions(cx-100,y+75,200,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Довідка"), b -> command("kingdom help")).dimensions(cx-100,y+100,200,20).build());
    }
    private void command(String cmd) { if(client!=null && client.player!=null) { client.player.networkHandler.sendChatCommand(cmd); close(); } }
    @Override public void render(DrawContext ctx,int mouseX,int mouseY,float delta) {
        renderBackground(ctx); ctx.fill(width/2-116,height/2-91,width/2+116,height/2+137,0xD0201A16);
        ctx.drawCenteredTextWithShadow(textRenderer,"MY KINGDOM",width/2,height/2-82,0xE6C46A);
        ctx.drawCenteredTextWithShadow(textRenderer,"Королівство починається з двох жителів",width/2,height/2-68,0xFFFFFF);
        super.render(ctx,mouseX,mouseY,delta);
    }
    @Override public boolean shouldPause() { return false; }
}
