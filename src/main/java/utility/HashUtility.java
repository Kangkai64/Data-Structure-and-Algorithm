package utility;

public class HashUtility {
    public static int hashEntity(int hashCode, int bucketCount) {
        long hash = 17;
        final int prime = 31;
        final int mod = 1000000007;

        hash = (hash * prime + hashCode) % mod;

        while (hash >= bucketCount) {
            hash = hash % bucketCount;
        }

        return (int) hash;
    }
}
