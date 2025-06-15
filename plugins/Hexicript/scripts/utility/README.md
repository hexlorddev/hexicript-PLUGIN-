# Hexicript Utility Scripts

This directory contains utility scripts that provide common functionality for Hexicript scripts. These scripts are designed to be reusable across different projects.

## Available Utilities

### Player Utils (`player_utils.hxs`)
Provides various utility functions for player management, including:
- Permission checks
- Display name formatting
- Player healing
- Safe item giving with inventory checks

## How to Use

To use these utilities in your scripts, simply include them at the top of your script:

```hxs
# Import player utilities
import "utility/player_utils.hxs"

# Now you can use the utility functions
on player join:
    # Heal the player
    heal_player(player, 20)
    
    # Give an item safely
    give_item_safe(player, "diamond_sword")
    
    # Check permission
    if has_permission(player, "my.permission"):
        send "You have the required permission!" to player
```

## Adding New Utilities

To add a new utility script:
1. Create a new `.hxs` file in this directory
2. Add your utility functions
3. Document the functions at the top of the file
4. Update this README with information about the new utility

## Best Practices

- Keep utility functions small and focused on a single task
- Document all functions with comments
- Use descriptive function and variable names
- Handle errors appropriately
- Consider performance implications
- Test thoroughly before using in production
