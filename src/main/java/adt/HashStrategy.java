package adt;

/**
 * Hash strategies for common attributes. These implement the
 * ArrayBucketList.HashFunction interface and can be plugged into
 * ArrayBucketList constructors.
 */
public enum HashStrategy implements ArrayBucketList.HashFunction<Object> {
    /**
     * Prefix-based name hashing. Intended to start with capacity 26 and grow.
     * The effective prefix length is floor(log_26(bucketCount)), min 1.
     * Non-letters are ignored in hashing.
     */
    NAME_PREFIX {
        @Override
        public int hash(Object key, int bucketCount) {
            String name = asString(key);
            if (name == null || name.isEmpty() || bucketCount <= 0) {
                return 0;
            }
            String normalized = name.toLowerCase().trim().replaceAll("[^a-z]", "");
            if (normalized.isEmpty()) {
                return 0;
            }
            int prefixLength = Math.max(1, effectivePrefixLength(bucketCount));
            int length = Math.min(prefixLength, normalized.length());
            int hash = 0;
            for (int index = 0; index < length; index++) {
                int value = normalized.charAt(index) - 'a'; // 0..25
                hash = hash * 26 + value;
            }
            return Math.floorMod(hash, bucketCount);
        }

        private int effectivePrefixLength(int bucketCount) {
            // floor(log_26(bucketCount))
            int length = 0;
            int capacity = bucketCount;
            while (capacity >= 26) {
                capacity /= 26;
                length++;
            }
            return Math.max(1, length);
        }
    },
    NAME {
        @Override
        public int hash(Object key, int bucketCount) {
            String name = asString(key);
            if (name == null || name.isEmpty()) {
                return 0;
            }
            String normalized = name.toLowerCase().trim();
            int hash = 0;
            for (int index = 0; index < Math.min(normalized.length(), 8); index++) {
                hash = hash * 37 + normalized.charAt(index);
            }
            return Math.abs(hash) % bucketCount;
        }
    },
    PHONE {
        @Override
        public int hash(Object key, int bucketCount) {
            String phone = asString(key);
            if (phone == null) {
                return 0;
            }
            String digits = phone.replaceAll("\\D", "");
            if (digits.isEmpty()) {
                return 0;
            }
            int hash = 0;
            int start = Math.max(0, digits.length() - 7);
            for (int index = start; index < digits.length(); index++) {
                hash = hash * 10 + (digits.charAt(index) - '0');
            }
            return Math.abs(hash) % bucketCount;
        }
    },
    EMAIL {
        @Override
        public int hash(Object key, int bucketCount) {
            String email = asString(key);
            if (email == null) {
                return 0;
            }
            String normalized = email.toLowerCase().trim();
            int hash = 0;
            int atIndex = normalized.indexOf('@');
            if (atIndex > 0) {
                for (int index = 0; index < atIndex; index++) {
                    hash = hash * 31 + normalized.charAt(index);
                }
                for (int index = atIndex + 1; index < normalized.length(); index++) {
                    hash = hash * 37 + normalized.charAt(index);
                }
            } else {
                for (int index = 0; index < normalized.length(); index++) {
                    hash = hash * 31 + normalized.charAt(index);
                }
            }
            return Math.abs(hash) % bucketCount;
        }
    },
    POSTCODE {
        @Override
        public int hash(Object key, int bucketCount) {
            String postcode = asString(key);
            if (postcode == null) {
                return 0;
            }
            String normalized = postcode.replaceAll("\\s+", "").toUpperCase();
            int hash = 0;
            for (int index = 0; index < normalized.length(); index++) {
                int weight = normalized.length() - index + 1;
                hash += weight * normalized.charAt(index);
            }
            return Math.abs(hash) % bucketCount;
        }
    };

    /**
     * Generic string hashing with additional mixing (ID-like strings).
     */
    public static final ArrayBucketList.HashFunction<Object> STRING_ID = (key, bucketCount) -> {
        String keyStr = asString(key);
        if (keyStr == null || bucketCount <= 0) {
            return 0;
        }
        int hash = 0;
        for (int index = 0; index < keyStr.length(); index++) {
            hash = hash * 31 + keyStr.charAt(index);
        }
        hash ^= (hash >>> 16);
        hash *= 0x85ebca6b;
        hash ^= (hash >>> 13);
        return Math.floorMod(hash, bucketCount);
    };

    /**
     * Enum hashing based on ordinal.
     */
    public static final ArrayBucketList.HashFunction<Object> ENUM = (key, bucketCount) -> {
        if (key instanceof Enum<?> enumKey) {
            return Math.floorMod(enumKey.ordinal(), Math.max(1, bucketCount));
        }
        return 0;
    };

    /**
     * java.time.LocalDate hashing based on epoch day.
     */
    public static final ArrayBucketList.HashFunction<Object> LOCAL_DATE = (key, bucketCount) -> {
        if (key instanceof java.time.LocalDate date) {
            long epochDay = date.toEpochDay();
            return (int) Math.floorMod(epochDay, Math.max(1, bucketCount));
        }
        return 0;
    };

    private static String asString(Object key) {
        return key == null ? null : key.toString();
    }
}


