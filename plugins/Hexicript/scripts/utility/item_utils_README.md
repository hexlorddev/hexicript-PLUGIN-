# Item Utilities

This script provides utility functions for item and inventory management in Hexicript.

## Features

- Create custom items with ease
- Compare and match items
- Manage player inventories
- Serialize and deserialize items
- Handle item enchantments and metadata
- Work with special items like colored armor and player heads

## Functions

### Item Creation
- `create_item(material, amount, name, lore, enchantments, flags, unbreakable, custom_model_data)` - Create a custom item
- `create_colored_leather_armor(type, color, name, lore)` - Create colored leather armor
- `create_player_head(player_name, name, lore)` - Create a player head with custom skin
- `create_banner(base_color, patterns, name, lore)` - Create a custom banner
- `create_firework(power, colors, fade_colors, effects, flicker, trail, name, lore)` - Create a custom firework

### Item Comparison
- `items_match(item1, item2, check_meta)` - Check if two items match
- `has_enchantment(item, enchantment, level)` - Check if an item has a specific enchantment
- `get_enchantment_level(item, enchantment)` - Get the level of an enchantment
- `has_durability(item)` - Check if an item has durability
- `get_durability_percentage(item)` - Get item's durability percentage

### Inventory Management
- `count_items(player, item, check_meta)` - Count items in a player's inventory
- `remove_items(player, item, amount, check_meta)` - Remove items from a player's inventory
- `give_item(player, item, drop_if_full)` - Give an item to a player with inventory checks

### Item Manipulation
- `add_enchantment(item, enchantment, level, ignore_restrictions)` - Add an enchantment to an item
- `remove_enchantment(item, enchantment)` - Remove an enchantment from an item
- `repair_item(item)` - Repair an item (set durability to max)

### Serialization
- `serialize_item(item)` - Convert an item to a string
- `deserialize_item(item_string)` - Convert a string back to an item

## Usage Examples

### Create a Custom Item
```hxs
# Create a diamond sword with custom name and lore
set {sword} to create_item(
    "DIAMOND_SWORD", 
    1, 
    "&6Legendary Sword", 
    ["&7A powerful weapon", "&eSpecial Ability: &cFire Aspect"],
    ["FIRE_ASPECT:2", "SHARPNESS:5"],
    ["HIDE_ENCHANTS", "HIDE_ATTRIBUTES"],
    true,
    1001
)

give {sword} to player
```

### Check for Items in Inventory
```hxs
# Create a simple diamond item for comparison
set {diamond} to new item of type "DIAMOND"

# Count how many diamonds the player has
set {count} to count_items(player, {diamond}, false)
send "You have %{count}% diamonds!" to player

# Remove 5 diamonds
set {removed} to remove_items(player, {diamond}, 5, false)
if {removed} > 0:
    send "Removed %{removed}% diamonds from your inventory!" to player
else:
    send "You don't have enough diamonds!" to player
```

### Serialize and Deserialize Items
```hxs
# Save an item to a string
set {item} to create_item("DIAMOND_PICKAXE", 1, "&bSuper Pickaxe")
set {serialized} to serialize_item({item})

# Later, convert it back to an item
set {new_item} to deserialize_item({serialized})
give {new_item} to player
```

### Create Special Items
```hxs
# Create colored leather armor
set {chestplate}, {error} to create_colored_leather_armor(
    "LEATHER_CHESTPLATE", 
    "#FF0000",  # Red color
    "&cRed Armor",
    ["&7Special red armor"]
)

# Create a player head
set {head} to create_player_head("Notch", "&6Notch's Head", ["&7Original creator"])

# Create a custom banner
set {banner} to create_banner("RED", [
    pattern "STRIPE_SMALL" color "WHITE",
    pattern "CROSS" color "BLACK"
], "&4Custom Banner")

# Create a firework
set {firework} to create_firework(
    2,  # Power (flight duration)
    ["#FF0000", "#00FF00"],  # Colors (red and green)
    ["#0000FF"],  # Fade colors (blue)
    ["BURST", "STAR"],  # Effects
    true,  # Flicker
    true,  # Trail
    "&aSpecial Firework"
)

give {chestplate} to player
give {head} to player
give {banner} to player
give {firework} to player
```

## Error Handling

Most functions that can fail will return a boolean indicating success and an optional error message:

```hxs
set {item}, {error} to create_colored_leather_armor("DIAMOND_CHESTPLATE", "#FF0000")
if {item} is null:
    send "Error: %{error}%" to player
else:
    give {item} to player
```

## Dependencies

- Requires Hexicript core functionality
- Some features may require specific Minecraft versions or server software
