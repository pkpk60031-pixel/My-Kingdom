package com.mykingdom;

import com.mykingdom.command.KingdomCommands;
import com.mykingdom.data.KingdomState;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyKingdom implements ModInitializer {
    public static final String MOD_ID = "mykingdom";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static long tickCounter;

    @Override
    public void onInitialize() {
        KingdomCommands.register();
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            ServerWorld world = player.getServerWorld();
            if (world.getRegistryKey() != World.OVERWORLD) return;
            KingdomState state = KingdomState.get(world);
            if (!state.isFounded()) {
                state.found(world, player.getBlockPos());
                state.markDirty();
                player.sendMessage(Text.literal("§6[My Kingdom] §fТвоє королівство засновано! Двоє перших жителів уже чекають на накази."), false);
            }
        });
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.getRegistryKey() != World.OVERWORLD) return;
            tickCounter++;
            if (tickCounter % 5 == 0) KingdomState.get(world).tickConstruction(world);
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);
        LOGGER.info("My Kingdom {} initialized", "0.1.0");
    }

    private void onServerStopping(MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) KingdomState.get(world).markDirty();
    }
}
