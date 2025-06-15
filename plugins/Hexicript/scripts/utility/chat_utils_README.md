# Chat Utilities

This script provides utility functions for chat and message formatting in Hexicript, making it easier to create rich, interactive chat interfaces.

## Features

- Send formatted messages to players
- Create progress bars and countdowns
- Generate scrolling text and typewriter effects
- Implement chat pagination
- Support for different message types (chat, action bar, title, etc.)
- Color code formatting

## Functions

### Message Sending
- `send_message(player, message, type, sound, pitch, volume)` - Send a formatted message to a player
- `broadcast_message(message, permission, type, sound, pitch, volume)` - Broadcast a message to all players

### Text Formatting
- `format_colors(text)` - Format color codes in a string
- `strip_colors(text)` - Remove color codes from a string
- `center_message(message, line_char, line_color)` - Center a message in chat
- `format_time(seconds, show_seconds)` - Format time in seconds to a human-readable format

### Visual Elements
- `create_progress_bar(current, max, length, filled_char, empty_char, filled_color, empty_color, brackets_color, show_percentage, show_numbers)` - Create a progress bar
- `create_countdown(duration, format, update_interval, on_finish, type, sound, finish_sound)` - Create a countdown timer
- `create_scrolling_text(text, width, speed, reverse)` - Create a scrolling text effect
- `create_typewriter_effect(text, speed)` - Create a typewriter effect

### Chat Pagination
- `create_paginated_chat(player, title, items, items_per_page, command)` - Create a paginated chat interface
- `show_page(player, title, page_number)` - Show a specific page of a paginated chat
- `handle_page_command(player, title, args)` - Handle page navigation commands
- `cleanup_pagination(player, title)` - Clean up pagination data

## Usage Examples

### Sending Messages
```hxs
# Send a simple chat message
send_message(player, "&aHello, &e%player%&a!", "chat", "ENTITY_PLAYER_LEVELUP", 1.0, 1.0)

# Send a title with subtitle
send_message(player, "&6Title\n&7Subtitle", "title_with_subtitle")

# Broadcast a message to all players with a permission
broadcast_message("&aAnnouncement: &7Server restart in 5 minutes!", "myplugin.announce")
```

### Creating Progress Bars
```hxs
# Create a simple health bar
set {health} to player's health
set {max_health} to player's max health
set {bar} to create_progress_bar({health}, {max_health}, 20, "|", " ", "&a", "&7", "&8", true, true)
send_message(player, "&c❤ &7Health: &f%{bar}%")

# Create an experience bar
set {exp} to player's exp
set {exp_to_level} to player's exp to level
set {bar} to create_progress_bar({exp}, {exp_to_level}, 30, "█", "░", "&b", "&8", "&7", true, false)
send_message(player, "&a✎ &7Experience: &f%{bar}%")
```

### Creating Countdowns
```hxs
# Create a 10-second countdown
set {countdown} to create_countdown(
    10,  # Duration in seconds
    "&eGame starting in &6%time%",  # Format
    1,  # Update interval
    "&aGame started!",  # Finish message
    "action_bar",  # Message type
    "BLOCK_NOTE_BLOCK_HAT",  # Tick sound
    "ENTITY_PLAYER_LEVELUP"  # Finish sound
)
```

### Creating Scrolling Text
```hxs
# Create a scrolling text
set {scrolling_text} to create_scrolling_text("&aWelcome to our server! ", 20, 1, false)

# In a repeating task, update the display
set {task} to run task every 5 ticks:
    set {frame} to get_scrolling_text_frame({scrolling_text})
    send_action_bar({frame} to player)
```

### Creating Paginated Chat
```hxs
# Create a list of items to display
set {items} to []
loop 50 times:
    add "&7Item &e%loop-number%" to {items}

# Create paginated chat
create_paginated_chat(
    player,
    "Item List",
    {items},
    8,  # Items per page
    "/items"  # Command to change pages
)

# Handle page navigation command
command "/items":
    execute:
        handle_page_command(player, "Item List", args)
```

## Color Codes

Hexicript supports both legacy color codes and hex color codes:

### Legacy Color Codes
- `&0` - Black
- `&1` - Dark Blue
- `&2` - Dark Green
- `&3` - Dark Aqua
- `&4` - Dark Red
- `&5` - Dark Purple
- `&6` - Gold
- `&7` - Gray
- `&8` - Dark Gray
- `&9` - Blue
- `&a` - Green
- `&b` - Aqua
- `&c` - Red
- `&d` - Light Purple
- `&e` - Yellow
- `&f` - White
- `&k` - Obfuscated
- `&l` - Bold
- `&m` - Strikethrough
- `&n` - Underline
- `&o` - Italic
- `&r` - Reset

### Hex Color Codes
Use the format `&#RRGGBB` to specify hex colors:
- `&#FF0000` - Red
- `&#00FF00` - Green
- `&#0000FF` - Blue
- `#FFA500` - Orange

## Dependencies

- Requires Hexicript core functionality
- Some features may require specific Minecraft versions
