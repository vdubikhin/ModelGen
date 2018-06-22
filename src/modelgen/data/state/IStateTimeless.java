package modelgen.data.state;

import modelgen.data.DataType;
import modelgen.data.complex.Printable;


public interface IStateTimeless extends Printable {

    String getSignalName();

    Integer getId();

    DataType getType();

    String convertToGuardCondition();

    String convertToAssignmentCondition();

    String convertToInitialCondition();

    String convertToInitialRateCondition();

    void setScale(double scale);

    Integer getScalePower();
}
