package zingg.common.core.block;

public class HashFunctionUtilityFactory {

    public static IHashFunctionUtility getHashFunctionUtility(HashUtility hashUtility) {

        if (HashUtility.DEFAULT.equals(hashUtility)) {
            return new DefaultHashFunctionUtility();
        }
        return new CacheBasedHashFunctionUtility();
    }
}
