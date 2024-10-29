package zingg.common.core.block;

public class HashFunctionUtilityFactory<D, R, C, T> {

    public IHashFunctionUtility<D, R, C, T> getHashFunctionUtility(HashUtility hashUtility) {

        if (HashUtility.DEFAULT.equals(hashUtility)) {
            return new DefaultHashFunctionUtility<D, R, C, T>();
        }
        return new CacheBasedHashFunctionUtility<D, R, C, T>();
    }
}
