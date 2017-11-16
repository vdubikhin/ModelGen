package modelFSM.data;

import java.util.ArrayList;

public abstract class ArrayListWrapper<T> extends ArrayList<T> {
    private static final long serialVersionUID = 1L;

    public ArrayListWrapper() {
        super();
    }

    public ArrayListWrapper(ArrayList<T> toCopy) {
        super(toCopy);
    }
}
