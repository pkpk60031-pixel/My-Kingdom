package com.mykingdom.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class MyKingdomClient implements ClientModInitializer {
    private static KeyBinding openKingdom;
    @Override public void onInitializeClient() {
        openKingdom = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.mykingdom.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "category.mykingdom"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKingdom.wasPressed()) if (client.player != null) client.setScreen(new KingdomScreen());
        });
    }
}
