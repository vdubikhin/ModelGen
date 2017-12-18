package modelgen.data.property;

public interface IProperty {

    String getName();

    Object getValue();

    boolean setValue(Object value);
}