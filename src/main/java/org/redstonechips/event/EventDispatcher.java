
package org.redstonechips.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

/**
 * Dispatches bukkit events dynamically to interested listeners. 
 * 
 * To start the dispatcher you need to bind it to a plugin using the bindToPlugin method.
 * Calling stop() unregisters all event listeners.
 * 
 * @author taleisenberg
 */
public class EventDispatcher { 
    private Plugin plugin;
    
    private final Map<Class<?extends Event>, List<EventListener>> listeners = new HashMap<>();

    /**
     * Register an EventListener to a specific bukkit event.
     * @param type The event type's class.
     * @param l The registered EventListener.
     */
    public void registerListener(Class<? extends Event> type, EventListener l) {
        if (listeners.containsKey(type)) {
            List<EventListener> eventListeners = listeners.get(type);
            eventListeners.add(l);
            
        } else {
            List<EventListener> eventListeners = new ArrayList<>();
            eventListeners.add(l);
            registerBukkitListener(type);
            listeners.put(type, eventListeners);
        }
    }
    
    /**
     * Unregisters an EventListener.
     * @param listener The unregistered listener
     * @return true when the listener is found in the registry or false otherwise.
     */
    public boolean unregisterListener(EventListener listener) {
        Set<Class<? extends Event>> events = lookupEventClasses(listener);
        for (Class<? extends Event> event : events) {
            List<EventListener> ls = listeners.get(event);
            if (ls.size()==1) {
                unregisterBukkitListener(event);
                listeners.remove(event);                
            } else {
                ls.remove(listener);
            }            
        }
        
        return (!events.isEmpty());
    }
    
    /**
     * Bind the dispatcher to a plugin and start processing events.
     * 
     * @param p The parent plugin of this dispatcher.
     */
    public void bindToPlugin(Plugin p) {
        this.plugin = p;
        
        for (Class<? extends Event> eventClass : listeners.keySet()) {
            registerBukkitListener(eventClass);
        }
    }
    
    /**
     * Unregister all bukkit event listeners and stop processing events.
     */
    public void stop() {
        for (Class<? extends Event> eventClass : listeners.keySet()) {
            unregisterBukkitListener(eventClass);
        }
    }
    
    private void registerBukkitListener(Class<? extends Event> eventClass) {
        if (plugin==null) return;
        
        plugin.getServer().getPluginManager().registerEvent(
                eventClass, 
                new Listener() {}, 
                EventPriority.NORMAL, 
                new DispatchExecutor(eventClass),
                plugin);
    }
    
    private void unregisterBukkitListener(Class<? extends Event> eventClass) {
        if (plugin==null) return;
        
        try {
            Method m = eventClass.getMethod("getHandlerList", new Class[0]);
            HandlerList h = (HandlerList)m.invoke(null);
            h.unregister(plugin);
        } catch (NoSuchMethodException|IllegalAccessException|InvocationTargetException e) {
            throw new RuntimeException("Error while unregistering event: " + e.getMessage());
        }
    }
    
    private Set<Class<? extends Event>> lookupEventClasses(EventListener eventListener) {
        Set<Class<? extends Event>> res = new HashSet<>();
        
        for (Class<? extends Event> e : listeners.keySet()) {
            for (EventListener l : listeners.get(e)) {
                if (eventListener==l) {
                    res.add(e);
                    break;
                }
            }
        }
        return res;
    }
    
    class DispatchExecutor implements EventExecutor {
        Class<? extends Event> eventClass;
    
        public DispatchExecutor(Class<? extends Event> eventClass) {
            this.eventClass = eventClass;
        }
    
        @Override
        public void execute(Listener l, Event event) throws EventException {
            for (EventListener el : listeners.get(eventClass)) {
                el.onEvent(event);
            }
        }        
    }
}

