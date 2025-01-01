package zingg.common.core.feature;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import zingg.common.client.FieldDefinition;
import zingg.common.core.ZinggException;

public abstract class FieldDataTypeTester<T> {

    @Test
    public void testWhenKnownDataType() throws Exception {
        FeatureFactory<T> featureFactory = getFeatureFactory();
        FieldDefinition fieldDefinition = new FieldDefinition();
        fieldDefinition.setFieldName("firstName");
        fieldDefinition.setDataType("String");

        Assertions.assertEquals(StringFeature.class, featureFactory.get(fieldDefinition.getDataType()).getClass());
    }

    @Test
    public void testThrowExceptionWhenUnknownDataType() {
        FeatureFactory<T> featureFactory = getFeatureFactory();
        FieldDefinition fieldDefinition = new FieldDefinition();
        fieldDefinition.setFieldName("firstName");
        fieldDefinition.setDataType("Enum");

        Assertions.assertThrows(ZinggException.class, () -> featureFactory.get(fieldDefinition.getDataType()));
    }

    public abstract FeatureFactory<T> getFeatureFactory();
}
