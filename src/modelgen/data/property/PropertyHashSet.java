package modelgen.data.property;

import java.util.HashSet;

public class PropertyHashSet<T> extends Property<HashSet<T>> {
    private final Class<T> hashType;

    public PropertyHashSet (String name, Class<T> listType) {
        super(name, null);
        this.hashType = listType;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected HashSet<T> assignValue(Object value) throws ClassCastException {
        // Do cast to HashSet first to check if value is ArrayList.
        // If not exception will be thrown
        HashSet<T> temp = (HashSet<T>) value;

        // If passed array has values check if they are of the appropriate type
        for(T e: temp){
            if (!e.getClass().equals(hashType)) {
                throw new ClassCastException("HashSet<" + e.getClass().getSimpleName() +
                        "> cannot be cast to HashSet<" + hashType.getSimpleName() + ">");
            }
        }

        return temp;
    }
}
