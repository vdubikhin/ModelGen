package modelgen.data.property;

import java.util.ArrayList;

public class PropertyArrayList<T> extends Property<ArrayList<T>> {
    private final Class<T> listType;

    public PropertyArrayList (String name, Class<T> listType) {
        super(name, null);
        this.listType = listType;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ArrayList<T> assignValue(Object value) throws ClassCastException {
        // Do cast to arraylist first to check if value is ArrayList.
        // If not exception will be thrown
        ArrayList<T> temp = (ArrayList<T>) value;

        // If passed array has values check if they are of the appropriate type
        for(T e: temp){
            if (!e.getClass().equals(listType)) {
                throw new ClassCastException("ArrayList<" + e.getClass().getSimpleName() +
                        "> cannot be cast to ArrayList<" + listType.getSimpleName() + ">");
            }
        }

        return temp;
    }
}
