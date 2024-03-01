package zingg.common.client.cols;

public class FieldDefinition implements Named {
    private String fieldName;

    @Override
    public String getName() {
        return fieldName;
    }

    @Override
    public void setName(String name) {
        this.fieldName = name;
    }

    public boolean isDontUse() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isDontUse'");
    }
}