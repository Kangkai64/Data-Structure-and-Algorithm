package adt;

/**
 * Factory helpers to create ArrayBucketList instances using predefined
 * hash strategies for common attributes.
 */
public final class ArrayBucketListFactory {

    private ArrayBucketListFactory() {}

    public static <K, V> ArrayBucketList<K, V> createForNames(int capacity) {
        return new ArrayBucketList<>(capacity, (ArrayBucketList.HashFunction<K>) (key, buckets) ->
                HashStrategy.NAME.hash(key, buckets));
    }

    public static <K, V> ArrayBucketList<K, V> createForNamePrefix(int capacity) {
        return new ArrayBucketList<>(capacity, (ArrayBucketList.HashFunction<K>) (key, buckets) ->
                HashStrategy.NAME_PREFIX.hash(key, buckets));
    }

    public static <K, V> ArrayBucketList<K, V> createForPhones(int capacity) {
        return new ArrayBucketList<>(capacity, (ArrayBucketList.HashFunction<K>) (key, buckets) ->
                HashStrategy.PHONE.hash(key, buckets));
    }

    public static <K, V> ArrayBucketList<K, V> createForEmails(int capacity) {
        return new ArrayBucketList<>(capacity, (ArrayBucketList.HashFunction<K>) (key, buckets) ->
                HashStrategy.EMAIL.hash(key, buckets));
    }

    public static <K, V> ArrayBucketList<K, V> createForPostcodes(int capacity) {
        return new ArrayBucketList<>(capacity, (ArrayBucketList.HashFunction<K>) (key, buckets) ->
                HashStrategy.POSTCODE.hash(key, buckets));
    }

    public static <K, V> ArrayBucketList<K, V> createForStringIds(int capacity) {
        return new ArrayBucketList<>(capacity, (ArrayBucketList.HashFunction<K>) (key, buckets) ->
                HashStrategy.STRING_ID.hash(key, buckets));
    }

    public static <K, V> ArrayBucketList<K, V> createForEnums(int capacity) {
        return new ArrayBucketList<>(capacity, (ArrayBucketList.HashFunction<K>) (key, buckets) ->
                HashStrategy.ENUM.hash(key, buckets));
    }

    public static <K, V> ArrayBucketList<K, V> createForLocalDates(int capacity) {
        return new ArrayBucketList<>(capacity, (ArrayBucketList.HashFunction<K>) (key, buckets) ->
                HashStrategy.LOCAL_DATE.hash(key, buckets));
    }
}


