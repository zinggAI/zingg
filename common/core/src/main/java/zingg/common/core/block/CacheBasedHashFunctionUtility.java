package zingg.common.core.block;

import zingg.common.client.FieldDefinition;
import zingg.common.core.hash.HashFunction;

import java.util.HashSet;
import java.util.Set;

public class CacheBasedHashFunctionUtility<D, R, C, T> implements IHashFunctionUtility<D, R, C, T> {

    private final Set<String> hashFunctionsInCurrentNodePath;
    private static final String DELIMITER = ":";

    public CacheBasedHashFunctionUtility() {
        this.hashFunctionsInCurrentNodePath = new HashSet<String>();
    }

    @Override
    public boolean isHashFunctionUsed(FieldDefinition fieldDefinition, HashFunction<D, R, C, T> hashFunction, Tree<Canopy<R>> tree, Canopy<R> node) {
        return hashFunctionsInCurrentNodePath.contains(getKey(fieldDefinition, hashFunction));
    }

    @Override
    public void addHashFunctionIfRequired(Canopy<R> node) {
        addHashFunctionInCurrentNodePath(node);
    }

    @Override
    public void removeHashFunctionIfRequired(Canopy<R> node) {
        removeHashFunctionInCurrentNodePath(node);
    }

    private void addHashFunctionInCurrentNodePath(Canopy<R> node) {
        this.hashFunctionsInCurrentNodePath.add(getKey(node.getContext(), node.getFunction()));
    }

    private void removeHashFunctionInCurrentNodePath(Canopy<R> node) {
        this.hashFunctionsInCurrentNodePath.remove(getKey(node.getContext(), node.getFunction()));
    }

    private String getKey(FieldDefinition fieldDefinition, HashFunction<D, R, C, T> hashFunction) {
        return fieldDefinition.getName() + DELIMITER + hashFunction.getName();
    }
}
