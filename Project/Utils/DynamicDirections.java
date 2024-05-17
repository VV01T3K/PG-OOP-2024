package Utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DynamicDirections {
    private static final Map<String, DynamicDirections> instances = new HashMap<>();
    private static int nextOrdinal = 0; // Step 1: Add a static variable to keep track of the next ordinal
    private final String name;
    private final int ordinal; // Step 2: Add an instance variable for the ordinal

    private DynamicDirections(String name) {
        this.name = name;
        this.ordinal = nextOrdinal++; // Step 3: Assign and increment the nextOrdinal in the constructor
    }

    public static DynamicDirections addInstance(String name) {
        DynamicDirections instance = new DynamicDirections(name);
        instances.put(name, instance);
        return instance;
    }

    public static void removeInstance(String name) {
        instances.remove(name);
        // Note: Removing an instance does not affect the ordinal values of existing
        // instances.
        // If maintaining a consistent ordinal sequence without gaps is necessary,
        // additional logic will be required.
    }

    public static DynamicDirections getInstance(String name) {
        return instances.get(name);
    }

    public static Map<String, DynamicDirections> getInstances() {
        return Collections.unmodifiableMap(instances);
    }

    public int ordinal() { // Step 4: Add a method to return the ordinal
        return ordinal;
    }

    @Override
    public String toString() {
        return name;
    }

    public static DynamicDirections[] values() {
        return instances.values().toArray(new DynamicDirections[0]);
    }

    public static void clear() {
        instances.clear(); // Clears all instances
        nextOrdinal = 0; // Resets the ordinal counter
    }

    // Static instances
    public static final DynamicDirections UP = addInstance("UP");
    public static final DynamicDirections DOWN = addInstance("DOWN");
    public static final DynamicDirections LEFT = addInstance("LEFT");
    public static final DynamicDirections RIGHT = addInstance("RIGHT");
    public static final DynamicDirections SELF = addInstance("SELF");
}