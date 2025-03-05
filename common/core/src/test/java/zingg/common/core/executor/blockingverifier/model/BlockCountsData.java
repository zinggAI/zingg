package zingg.common.core.executor.blockingverifier.model;

public class BlockCountsData {

    public final Integer z_hash;
    public final Long z_hash_count;

    public BlockCountsData(Integer z_hash, Long z_hash_count){
        this.z_hash = z_hash;
        this.z_hash_count = z_hash_count;
    }
    
}
