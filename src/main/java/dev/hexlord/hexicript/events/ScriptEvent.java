package dev.hexlord.hexicript.events;

import dev.hexlord.hexicript.core.script.Script;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for all script-related events
 */
public abstract class ScriptEvent extends Event {
    
    private static final HandlerList handlers = new HandlerList();
    private final Script script;
    
    public ScriptEvent(Script script) {
        this.script = script;
    }
    
    /**
     * Get the script associated with this event
     */
    public Script getScript() {
        return script;
    }
    
    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}

/**
 * Called when a script is loaded
 */
class ScriptLoadEvent extends ScriptEvent {
    public ScriptLoadEvent(Script script) {
        super(script);
    }
}

/**
 * Called when a script is unloaded
 */
class ScriptUnloadEvent extends ScriptEvent {
    public ScriptUnloadEvent(Script script) {
        super(script);
    }
}

/**
 * Called when a script is executed
 */
class ScriptExecuteEvent extends ScriptEvent {
    private final String[] args;
    
    public ScriptExecuteEvent(Script script, String[] args) {
        super(script);
        this.args = args;
    }
    
    /**
     * Get the arguments passed to the script
     */
    public String[] getArgs() {
        return args;
    }
}

/**
 * Called when a script throws an error
 */
class ScriptErrorEvent extends ScriptEvent {
    private final Throwable error;
    
    public ScriptErrorEvent(Script script, Throwable error) {
        super(script);
        this.error = error;
    }
    
    /**
     * Get the error that was thrown
     */
    public Throwable getError() {
        return error;
    }
}
