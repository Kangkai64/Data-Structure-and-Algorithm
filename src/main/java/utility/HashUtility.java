package utility;

public class HashUtility {
    public static int hashEntity(String hashData, int bucketCount) {
        long hash = 0;
        final int prime = 31;
        final int mod = 1000000007; // Large prime for modulo

        // Normalize and combine all fields
        String normalized = hashData.replaceAll("[^0-9]", "");

        for (int index = 0; index < normalized.length(); index++) {
            hash = (hash * prime + normalized.charAt(index)) % mod;
        }

        // Ensure the hash is within the bucket count
        while (hash >= bucketCount) {
            hash = hash % bucketCount;
        }

        return (int) hash;
    }
}
