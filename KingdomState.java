package com.mykingdom.data;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Persistent per-world kingdom state. Mutations are server-thread only. */
public class KingdomState extends PersistentState {
    public static final String KEY = "mykingdom_state";
    private boolean founded;
    private int treasury = 100;
    private int taxRate = 10;
    private int population;
    private final List<CitizenRecord> citizens = new ArrayList<>();
    private final List<ConstructionTask> construction = new ArrayList<>();

    public static KingdomState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(KingdomState::fromNbt, KingdomState::new, KEY);
    }
    public boolean isFounded() { return founded; }
    public int getTreasury() { return treasury; }
    public int getPopulation() { return population; }
    public int getTaxRate() { return taxRate; }
    public List<CitizenRecord> getCitizens() { return List.copyOf(citizens); }

    public void found(ServerWorld world, BlockPos center) {
        if (founded) return;
        founded = true;
        spawnFounder(world, center.add(2, 0, 2), "Адріан", "builder");
        spawnFounder(world, center.add(-2, 0, 2), "Еліна", "farmer");
        population = citizens.size();
        markDirty();
    }
    private void spawnFounder(ServerWorld world, BlockPos pos, String name, String profession) {
        VillagerEntity villager = EntityType.VILLAGER.create(world);
        if (villager == null) return;
        villager.refreshPositionAndAngles(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 0, 0);
        villager.setCustomName(Text.literal(name));
        villager.setCustomNameVisible(true);
        villager.setPersistent();
        if (world.spawnEntity(villager)) citizens.add(new CitizenRecord(villager.getUuid(), name, profession, 18 + world.random.nextInt(25), 100, 100, 0));
    }
    public boolean setProfession(String name, String profession) {
        profession = profession.toLowerCase();
        if (!isValidProfession(profession)) return false;
        for (CitizenRecord c : citizens) if (c.name.equalsIgnoreCase(name)) { c.profession = profession; markDirty(); return true; }
        return false;
    }
    public boolean changeTax(int rate) { if (rate < 0 || rate > 50) return false; taxRate = rate; markDirty(); return true; }
    public boolean addCoins(int amount) { if (amount <= 0 || amount > 100000) return false; treasury += amount; markDirty(); return true; }
    public boolean spendCoins(int amount) { if (amount < 0 || treasury < amount) return false; treasury -= amount; markDirty(); return true; }
    public boolean startBuilding(String type, BlockPos origin) {
        Blueprint bp = Blueprint.create(type);
        if (bp == null || construction.size() >= 4 || !spendCoins(bp.cost)) return false;
        construction.add(new ConstructionTask(type, origin.toImmutable(), 0)); markDirty(); return true;
    }
    public void tickConstruction(ServerWorld world) {
        if (construction.isEmpty()) return;
        ConstructionTask task = construction.get(0); Blueprint bp = Blueprint.create(task.type);
        if (bp == null || task.progress >= bp.blocks.size()) { construction.remove(0); markDirty(); return; }
        Placement p = bp.blocks.get(task.progress); BlockPos target = task.origin.add(p.x, p.y, p.z);
        if (!world.getWorldBorder().contains(target)) { construction.remove(0); markDirty(); return; }
        if (!world.getBlockState(target).isReplaceable()) return;
        world.setBlockState(target, p.state, 3); task.progress++; markDirty();
    }
    public static boolean isValidProfession(String p) {
        return List.of("builder","farmer","miner","lumberjack","blacksmith","merchant","soldier","archer","doctor","cook","commander").contains(p.toLowerCase());
    }
    public static String professionLabel(String p) {
        return switch (p) {
            case "builder" -> "Будівельник"; case "farmer" -> "Фермер"; case "miner" -> "Шахтар";
            case "lumberjack" -> "Лісоруб"; case "blacksmith" -> "Коваль"; case "merchant" -> "Торговець";
            case "soldier" -> "Солдат"; case "archer" -> "Лучник"; case "doctor" -> "Лікар";
            case "cook" -> "Кухар"; case "commander" -> "Командир"; default -> p;
        };
    }
    @Override public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putBoolean("Founded", founded); nbt.putInt("Treasury", treasury); nbt.putInt("TaxRate", taxRate); nbt.putInt("Population", population);
        NbtList list = new NbtList(); for (CitizenRecord c : citizens) list.add(c.write()); nbt.put("Citizens", list);
        NbtList tasks = new NbtList(); for (ConstructionTask t : construction) tasks.add(t.write()); nbt.put("Construction", tasks);
        return nbt;
    }
    public static KingdomState fromNbt(NbtCompound nbt) {
        KingdomState s = new KingdomState(); s.founded=nbt.getBoolean("Founded"); s.treasury=nbt.getInt("Treasury"); s.taxRate=nbt.getInt("TaxRate"); s.population=nbt.getInt("Population");
        NbtList cs=nbt.getList("Citizens", NbtElement.COMPOUND_TYPE); for(int i=0;i<cs.size();i++) s.citizens.add(CitizenRecord.read(cs.getCompound(i)));
        NbtList ts=nbt.getList("Construction", NbtElement.COMPOUND_TYPE); for(int i=0;i<ts.size();i++) s.construction.add(ConstructionTask.read(ts.getCompound(i)));
        return s;
    }
    public static final class CitizenRecord {
        public final UUID uuid; public final String name; public String profession; public final int age; public int health; public int hunger; public int experience;
        CitizenRecord(UUID id,String name,String prof,int age,int hp,int hunger,int xp){this.uuid=id;this.name=name;this.profession=prof;this.age=age;this.health=hp;this.hunger=hunger;this.experience=xp;}
        NbtCompound write(){NbtCompound n=new NbtCompound();n.putUuid("UUID",uuid);n.putString("Name",name);n.putString("Profession",profession);n.putInt("Age",age);n.putInt("Health",health);n.putInt("Hunger",hunger);n.putInt("Experience",experience);return n;}
        static CitizenRecord read(NbtCompound n){return new CitizenRecord(n.getUuid("UUID"),n.getString("Name"),n.getString("Profession"),n.getInt("Age"),n.getInt("Health"),n.getInt("Hunger"),n.getInt("Experience"));}
    }
    private static final class ConstructionTask {
        final String type; final BlockPos origin; int progress;
        ConstructionTask(String type,BlockPos origin,int progress){this.type=type;this.origin=origin;this.progress=progress;}
        NbtCompound write(){NbtCompound n=new NbtCompound();n.putString("Type",type);n.putLong("Origin",origin.asLong());n.putInt("Progress",progress);return n;}
        static ConstructionTask read(NbtCompound n){return new ConstructionTask(n.getString("Type"),BlockPos.fromLong(n.getLong("Origin")),n.getInt("Progress"));}
    }
    private record Placement(int x,int y,int z,BlockState state) {}
    private static final class Blueprint {
        final int cost; final List<Placement> blocks=new ArrayList<>(); Blueprint(int cost){this.cost=cost;}
        static Blueprint create(String id){
            Blueprint b=new Blueprint(id.equals("house")?35:id.equals("farm")?20:60);
            if(id.equals("house")){
                for(int x=0;x<5;x++)for(int z=0;z<5;z++)b.blocks.add(new Placement(x,0,z,Blocks.OAK_PLANKS.getDefaultState()));
                for(int y=1;y<=3;y++)for(int i=0;i<5;i++){if(i!=2||y!=1)b.blocks.add(new Placement(0,y,i,Blocks.OAK_PLANKS.getDefaultState()));b.blocks.add(new Placement(4,y,i,Blocks.OAK_PLANKS.getDefaultState()));b.blocks.add(new Placement(i,y,0,Blocks.OAK_PLANKS.getDefaultState()));b.blocks.add(new Placement(i,y,4,Blocks.OAK_PLANKS.getDefaultState()));}
                for(int x=0;x<5;x++)for(int z=0;z<5;z++)b.blocks.add(new Placement(x,4,z,Blocks.OAK_PLANKS.getDefaultState())); return b;
            }
            if(id.equals("farm")){for(int x=0;x<7;x++)for(int z=0;z<7;z++)b.blocks.add(new Placement(x,0,z,Blocks.FARMLAND.getDefaultState()));return b;}
            if(id.equals("watchtower")){for(int y=0;y<7;y++)for(int x=0;x<3;x++)for(int z=0;z<3;z++)if(x==0||x==2||z==0||z==2)b.blocks.add(new Placement(x,y,z,Blocks.COBBLESTONE.getDefaultState()));return b;}
            return null;
        }
    }
}
