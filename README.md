# TransportBukkit

TransportBukkit is a plugin that allows you to add custom vehicles to your server.

## Features

- [x] Custom vehicles
- [x] Yaw/Pitch/Roll support
- [x] Seat management
- [x] Animation support
- [x] Move a vehicle to a specific location

## Requirements

This plugin runs on Bukkit/Spigot/Paper 1.16.5 and above.
- [CommandAPI](https://commandapi.jorel.dev/)
- [ProtocolLib](https://github.com/dmulloy2/ProtocolLib/releases)

## Commands

The plugin supports Brigadier, so you can use tab completion and `/execute ... run ...`.

The plugin is reloadable, which means /reload won't break it (but avoid using /reload, use `/transport reload` instead).

### Creating a vehicle

`/transport create <template name> <name>`<br/>

### Removing a vehicle

`/transport delete <name>`<br/>

### List

`/transport list part_templates`<br/>
`/transport list vehicle_templates`<br/>
`/transport list vehicles`<br/>

### Info

`/transport info part_template <name>`<br/>
`/transport info vehicle_template <name>`<br/>
`/transport info part <name>`<br/>
`/transport info vehicle <name>`<br/>
`/transport info player <name>`<br/>

### Teleport to a vehicle

`/transport tpto <name>`<br/>

### Teleport a vehicle to you

`/transport tphere <name>`<br/>

### Sit in a vehicle (also works with entities)

`/transport sit enter <name>`<br/>
`/transport sit exit`<br/>
`/execute as @e[type=villager,distance=..5] run transport sit enter <name>`<br/>

### Lock a vehicle (prevent players from entering / exiting)

`/transport sit lock <name>`<br/>
`/transport sit unlock <name>`<br/>

### Move a vehicle to a specific location

`/transport movement move_here <name> (x) (y) (z) (yaw) (pitch) (roll)`<br/>
`/transport movement stop <name>`<br/>
