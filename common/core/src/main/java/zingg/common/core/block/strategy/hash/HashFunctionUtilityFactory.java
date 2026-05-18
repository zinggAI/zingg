package zingg.common.core.block.strategy.hash;

public class HashFunctionUtilityFactory {

    public static IHashFunctionUtility getHashFunctionUtility(HashUtility hashUtility) {

        if (HashUtility.DEFAULT.equals(hashUtility)) {
            return new DefaultHashFunctionUtility();
        }
        return new CacheBasedHashFunctionUtility();
    }
}
