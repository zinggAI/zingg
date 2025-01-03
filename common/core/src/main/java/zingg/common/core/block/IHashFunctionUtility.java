package zingg.common.core.block;

import zingg.common.client.FieldDefinition;
import zingg.common.core.hash.HashFunction;

public interface IHashFunctionUtility<D, R, C, T> {
    boolean isHashFunctionUsed(FieldDefinition fieldDefinition, HashFunction<D, R, C, T> hashFunction, Tree<Canopy<R>> tree, Canopy<R>node);

    void addHashFunctionIfRequired(Canopy<R> node);

    void removeHashFunctionIfRequired(Canopy<R> node);
}
