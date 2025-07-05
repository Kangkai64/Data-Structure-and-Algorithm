//package utility;
//
//import java.util.Arrays;
//import java.util.Comparator;
//
//import pchub.model.Product;
//
//public class ProductSorter {
//    public enum SortCriteria {
//        NAME_ASC,
//        NAME_DESC,
//        PRICE_ASC,
//        PRICE_DESC,
//        STOCK_ASC,
//        STOCK_DESC,
//        CATEGORY_ASC,
//        CATEGORY_DESC
//    }
//
//    public static Product[] sortProducts(Product[] products, SortCriteria criteria) {
//        if (products == null || products.length == 0) {
//            return products;
//        }
//
//        // Create a copy of the array to avoid modifying the original
//        Product[] sortedProducts = products.clone();
//
//        // Remove null values from the array
//        sortedProducts = Arrays.stream(sortedProducts)
//                .filter(product -> product != null)
//                .toArray(Product[]::new);
//
//        if (sortedProducts.length == 0) {
//            return products; // Return original array if all products were null
//        }
//
//        // Sort based on the specified criteria
//        switch (criteria) {
//            case NAME_ASC:
//                Arrays.sort(sortedProducts, Comparator.comparing(Product::getName, Comparator.nullsLast(String::compareTo)));
//                break;
//            case NAME_DESC:
//                Arrays.sort(sortedProducts, Comparator.comparing(Product::getName, Comparator.nullsLast(String::compareTo)).reversed());
//                break;
//            case PRICE_ASC:
//                Arrays.sort(sortedProducts, Comparator.comparing(Product::getUnitPrice, Comparator.nullsLast(Double::compareTo)));
//                break;
//            case PRICE_DESC:
//                Arrays.sort(sortedProducts, Comparator.comparing(Product::getUnitPrice, Comparator.nullsLast(Double::compareTo)).reversed());
//                break;
//            case STOCK_ASC:
//                Arrays.sort(sortedProducts, Comparator.comparing(Product::getCurrentQuantity, Comparator.nullsLast(Integer::compareTo)));
//                break;
//            case STOCK_DESC:
//                Arrays.sort(sortedProducts, Comparator.comparing(Product::getCurrentQuantity, Comparator.nullsLast(Integer::compareTo)).reversed());
//                break;
//            case CATEGORY_ASC:
//                Arrays.sort(sortedProducts, Comparator.comparing(Product::getCategory, Comparator.nullsLast(String::compareTo)));
//                break;
//            case CATEGORY_DESC:
//                Arrays.sort(sortedProducts, Comparator.comparing(Product::getCategory, Comparator.nullsLast(String::compareTo)).reversed());
//                break;
//        }
//
//        return sortedProducts;
//    }
//}