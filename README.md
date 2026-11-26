# My Kingdom — Fabric 1.20.1

Version: **0.1.1**. This project deliberately uses **ordinary vanilla Minecraft Villager entities**. It does not register a custom NPC/entity type and does not add custom villager geometry, textures, or animation rigs. Villagers retain the standard Minecraft model, skin variants, movement, and vanilla animations. Kingdom professions are separate mod data and do not replace the vanilla entity type.

## Toolchain

- Minecraft Java Edition: 1.20.1
- Java target: 17
- Fabric Loader: 0.20.1 (declared project dependency)
- Fabric API: 0.92.12+1.20.1
- Fabric Loom: 1.6.12
- Yarn mappings: 1.20.1+build.10
- Gradle wrapper distribution: 8.6

The first build requires network access so Gradle can download its distribution and Loom/Minecraft/Fabric dependencies.

## Implemented foundation

- Creates a per-world kingdom on first Overworld join.
- Spawns two persistent **vanilla VillagerEntity** villagers named Адріан and Еліна.
- Stores citizen records (UUID, name, age, health, hunger, profession, experience) in world PersistentState NBT.
- Stores treasury, tax rate, population, and construction queue in world PersistentState NBT.
- Supports 11 kingdom profession IDs and changing a citizen's kingdom profession.
- Has a client-side kingdom screen (press **K**) with command buttons.
- Has a staged block-placement queue for a small house, farm, and watchtower. Current construction is a basic persisted blueprint queue; it is not yet villager-driven material gathering/hauling.
- Includes commands for kingdom summary, citizens, profession changes, construction, tax, and treasury testing.

## Profession IDs

`builder`, `farmer`, `miner`, `lumberjack`, `blacksmith`, `merchant`, `soldier`, `archer`, `doctor`, `cook`, `commander`.

## Commands

- `/kingdom` — kingdom summary
- `/kingdom help` — command reference
- `/kingdom citizens` — citizen list
- `/kingdom profession <citizen> <profession>` — change a citizen's kingdom profession
- `/kingdom build <house|farm|watchtower>` — enqueue a blueprint
- `/kingdom tax <0..50>` — set tax rate (permission level 2)
- `/kingdom treasury add <amount>` — add test currency (permission level 2)

## Build

Install JDK 17 or newer and run from this directory:

```bash
./gradlew build
```

Windows: `gradlew.bat build`. The remapped mod JAR should be generated in `build/libs/` after a successful build.

## Install

1. Install Minecraft Java 1.20.1.
2. Install Fabric Loader for Minecraft 1.20.1.
3. Put Fabric API `0.92.12+1.20.1` in the instance's `mods` folder.
4. Put the generated `mykingdom-0.1.1.jar` in the same folder.
5. Launch the Fabric 1.20.1 profile and create/open a world.
6. Press **K** to open the kingdom panel.

## Explicit design decisions

- Citizens are normal vanilla villagers. Their standard model and vanilla animations are used.
- The mod's profession, age, needs, experience, and inventory concepts are kingdom-level data; they are not new Minecraft entity types.
- Future work AI and combat behavior will be implemented around vanilla villagers using server-side goals/events and carefully controlled commands.

## Not implemented yet

- Real villager AI for collecting, transporting, and consuming construction materials.
- Full individual inventory and needs simulation synchronized with vanilla villager behavior.
- All building blueprints, material shortage handling, doors/windows/roofs, and builder task assignment.
- Family relationships, births, child growth, housing capacity, and population simulation.
- Production chains, wages, markets, storage networks, trade UI, and tax collection.
- Combat AI, formations, cavalry, sieges, territorial control, destruction/repair, and military orders.
- AI-controlled foreign kingdoms, diplomacy, alliances, treaties, and peace negotiations.
- Dedicated multiplayer permission model and automated in-game tests.

## Build verification

The project files and archive can be inspected locally. A full Gradle/Loom compilation has **not** been verified in the current environment: the Gradle wrapper attempted to download Gradle 8.6, but DNS/network access to `services.gradle.org` was unavailable. Therefore no compiled JAR is included or claimed as tested.
