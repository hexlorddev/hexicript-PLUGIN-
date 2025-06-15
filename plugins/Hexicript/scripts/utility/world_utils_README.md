# World Utilities

This script provides utility functions for managing and interacting with Minecraft worlds in Hexicript.

## Features

- Create and delete worlds
- Manage world time and weather
- Get detailed world information
- Create and restore world backups
- List all available worlds
- Teleport players between worlds

## Functions

### World Management
- `create_world(name, environment, generator, seed)` - Create a new world
- `delete_world(name, remove_files)` - Delete a world
- `list_worlds()` - Get a list of all worlds
- `backup_world(world_name, backup_name)` - Create a backup of a world
- `restore_world(backup_name, world_name)` - Restore a world from backup

### World Information
- `get_world_info(world_name)` - Get detailed information about a world
- `format_world_info(world_name, include_players)` - Format world information as text
- `format_world_list(include_players)` - Get a formatted list of all worlds

### World Manipulation
- `set_world_time(world_name, time)` - Set the time in a world
- `set_world_weather(world_name, weather, duration)` - Set the weather in a world
- `teleport_all_players(from_world, to_world, to_location)` - Teleport players between worlds

## Usage Examples

### Create a New World
```hxs
# Create a normal world
set {success}, {message} to create_world("my_world")
if {success}:
    send "World created successfully!" to player
else:
    send "Error: %{message}%" to player

# Create a nether world
set {success}, {message} to create_world("nether_world", "NETHER")
```

### Manage World Time and Weather
```hxs
# Set time to day
set_world_time("world", "day")

# Set custom time (0-24000)
set_world_time("world", 6000)  # Noon

# Set weather to storm for 5 minutes
set_world_weather("world", "storm", 300)
```

### Get World Information
```hxs
# Get formatted world info
set {info} to format_world_info("world", true)
loop {info}:
    send loop-value to player
```

### Backup and Restore
```hxs
# Create a backup
set {success}, {message} to backup_world("world")
send {message} to player

# Restore from backup
set {success}, {message} to restore_world("world_backup_2023-01-01")
send {message} to player
```

### List All Worlds
```hxs
set {world_list} to format_world_list(true)
loop {world_list}:
    send loop-value to player
```

## Error Handling

All functions that can fail will return two values:
1. A boolean indicating success or failure
2. A message with details about the operation

Always check the success value before proceeding:
```hxs
set {success}, {message} to create_world("new_world")
if not {success}:
    send "Error: %{message}%" to player
    stop
```

## Dependencies

- Requires Hexicript core functionality
- Some features may require specific permissions or server settings
