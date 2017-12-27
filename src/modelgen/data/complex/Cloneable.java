package modelgen.data.complex;

public interface Cloneable<T extends Cloneable<T>> {
    T makeCopy();
}
