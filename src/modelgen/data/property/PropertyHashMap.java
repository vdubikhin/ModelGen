package modelgen.data.property;

import java.util.HashMap;

public class PropertyHashMap<K, V> extends Property<HashMap<K, V>> {
    private final Class<K> keyType;
    private final Class<V> ValueType;

    public PropertyHashMap (String name, Class<K> keyType, Class<V> ValueType) {
        super(name, null);
        this.keyType = keyType;
        this.ValueType = ValueType;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected HashMap<K, V> assignValue(Object value) throws ClassCastException {
        // Do cast to arraylist first to check if value is ArrayList.
        // If not exception will be thrown
        HashMap<K, V> temp = (HashMap<K, V>) value;
        
        // Check hashmap key types
        for(K e: temp.keySet()) {
            if (!e.getClass().equals(keyType)) {
                throw new ClassCastException("HashMap<" + e.getClass().getSimpleName() +
                        ",?> cannot be cast to HashMap<" + keyType.getSimpleName() + ",?>");
            }
        }

        // Check hashmap value types
        for(V e: temp.values()){
            if (!e.getClass().equals(ValueType)) {
                throw new ClassCastException("HashMap<?," + e.getClass().getSimpleName() +
                        "> cannot be cast to HashMap<?," + ValueType.getSimpleName() + ">");
            }
        }

        return temp;
    }
}
