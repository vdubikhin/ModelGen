package modelgen.data.state;

public interface IState {
    String convertToString();

    String getSignalName();

    Integer getId();
}
