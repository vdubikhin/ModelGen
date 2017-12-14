package modelgen.processor.discretization;


import modelgen.data.property.Properties;
import modelgen.data.raw.RawDataChunkGrouped;
import modelgen.processor.IDataProcessor;

class DiscretizeDataByValues implements IDataProcessor<DataOutput> {

    private RawDataChunkGrouped groupedData;
    
    public DiscretizeDataByValues(DataInput groupedData) {
        //this.groupedData = groupedData;
    }

    @Override
    public boolean setProcessorProperties(Properties properties) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Properties getProcessorProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int processCost() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public DataOutput processData() {
        // TODO Auto-generated method stub
        return null;
    }
    

}
