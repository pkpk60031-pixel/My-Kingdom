package com.mykingdom.command;

import com.mykingdom.data.KingdomState;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

public final class KingdomCommands {
    private KingdomCommands() {}
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
            CommandManager.literal("kingdom")
                .executes(ctx -> status(ctx.getSource()))
                .then(CommandManager.literal("help").executes(ctx -> help(ctx.getSource())))
                .then(CommandManager.literal("citizens").executes(ctx -> citizens(ctx.getSource())))
                .then(CommandManager.literal("profession")
                    .then(CommandManager.argument("citizen", StringArgumentType.word())
                        .then(CommandManager.argument("profession", StringArgumentType.word())
                            .executes(ctx -> profession(ctx.getSource(), StringArgumentType.getString(ctx,"citizen"), StringArgumentType.getString(ctx,"profession"))))))
                .then(CommandManager.literal("build")
                    .then(CommandManager.argument("building", StringArgumentType.word())
                        .executes(ctx -> build(ctx.getSource(), StringArgumentType.getString(ctx,"building")))))
                .then(CommandManager.literal("tax")
                    .then(CommandManager.argument("rate", IntegerArgumentType.integer(0,50))
                        .requires(src -> src.hasPermissionLevel(2))
                        .executes(ctx -> tax(ctx.getSource(), IntegerArgumentType.getInteger(ctx,"rate")))))
                .then(CommandManager.literal("treasury")
                    .then(CommandManager.literal("add")
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(1,100000))
                            .requires(src -> src.hasPermissionLevel(2))
                            .executes(ctx -> addCoins(ctx.getSource(), IntegerArgumentType.getInteger(ctx,"amount"))))))
        ));
    }
    private static int status(ServerCommandSource src) {
        KingdomState s=KingdomState.get(src.getWorld());
        src.sendFeedback(() -> Text.literal("§6§lMY KINGDOM\n§fНаселення: §a"+s.getPopulation()+"\n§fСкарбниця: §e"+s.getTreasury()+" монет\n§fПодатки: §e"+s.getTaxRate()+"%\n§fЖителі: §b/kingdom citizens\n§fБудівництво: §b/kingdom build house|farm|watchtower"),false);
        return 1;
    }
    private static int help(ServerCommandSource src) {
        src.sendFeedback(() -> Text.literal("§6My Kingdom — команди\n§e/kingdom §7— огляд королівства\n§e/kingdom citizens §7— список жителів\n§e/kingdom profession <ім'я> <професія> §7— призначити професію\n§e/kingdom build <house|farm|watchtower> §7— почати будівництво\n§e/kingdom tax <0..50> §7— податки (адмін)\n§e/kingdom treasury add <сума> §7— додати монети (адмін)\n§7Професії: builder, farmer, miner, lumberjack, blacksmith, merchant, soldier, archer, doctor, cook, commander"),false);
        return 1;
    }
    private static int citizens(ServerCommandSource src) {
        KingdomState s=KingdomState.get(src.getWorld());
        src.sendFeedback(() -> Text.literal("§6Жителі королівства:"),false);
        for (KingdomState.CitizenRecord c:s.getCitizens()) src.sendFeedback(() -> Text.literal("§f• §b"+c.name+" §7| "+KingdomState.professionLabel(c.profession)+" | вік "+c.age+" | HP "+c.health+" | голод "+c.hunger+" | XP "+c.experience),false);
        return s.getCitizens().size();
    }
    private static int profession(ServerCommandSource src,String name,String profession) {
        boolean ok=KingdomState.get(src.getWorld()).setProfession(name,profession);
        if(ok) src.sendFeedback(() -> Text.literal("§aПрофесію жителя "+name+" змінено на "+KingdomState.professionLabel(profession)+"."),false);
        else src.sendError(Text.literal("Не знайдено жителя або невідома професія. Використай /kingdom help."));
        return ok?1:0;
    }
    private static int build(ServerCommandSource src,String type) {
        if(!src.isExecutedByPlayer()) { src.sendError(Text.literal("Цю команду може виконати лише гравець.")); return 0; }
        BlockPos origin=src.getPlayer().getBlockPos().add(3,0,3);
        boolean ok=KingdomState.get(src.getWorld()).startBuilding(type,origin);
        if(ok) src.sendFeedback(() -> Text.literal("§aБудівельний майданчик створено: "+type+". Блоки встановлюватимуться поступово. Вартість списана зі скарбниці."),false);
        else src.sendError(Text.literal("Не вдалося почати будівництво. Перевір назву, баланс (до 4 активних завдань)."));
        return ok?1:0;
    }
    private static int tax(ServerCommandSource src,int rate) {
        KingdomState.get(src.getWorld()).changeTax(rate);
        src.sendFeedback(() -> Text.literal("§aСтавку податку встановлено: "+rate+"%."),true); return 1;
    }
    private static int addCoins(ServerCommandSource src,int amount) {
        KingdomState.get(src.getWorld()).addCoins(amount);
        src.sendFeedback(() -> Text.literal("§aДо скарбниці додано "+amount+" монет."),true); return 1;
    }
}
