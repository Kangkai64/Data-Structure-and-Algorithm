//package boundary;
//
//import java.util.Scanner;
//
///**
// *
// * @author Kat Tan
// */
//public class ProductMaintenanceUI {
//
//  Scanner scanner = new Scanner(System.in);
//
//  public int getMenuChoice() {
//    System.out.println("\nMAIN MENU");
//    System.out.println("1. Add new product");
//    System.out.println("2. List all products");
//    System.out.println("0. Quit");
//    System.out.print("Enter choice: ");
//    int choice = scanner.nextInt();
//    scanner.nextLine();
//    System.out.println();
//    return choice;
//  }
//
//  public void listAllProducts(String outputStr) {
//    System.out.println("\nList of Products:\n" + outputStr);
//  }
//
//  public void printProductDetails(Product product) {
//    System.out.println("Product Details");
//    System.out.println("Product code:" + product.getNumber());
//    System.out.println("Product name: " + product.getName());
//    System.out.println("Quantity: " + product.getQuantity());
//  }
//
//  public String inputProductCode() {
//    System.out.print("Enter product code: ");
//    String code = scanner.nextLine();
//    return code;
//  }
//
//  public String inputProductName() {
//    System.out.print("Enter product name: ");
//    String name = scanner.nextLine();
//    return name;
//  }
//
//  public int inputQuantity() {
//    System.out.print("Enter quantity: ");
//    int quantity = scanner.nextInt();
//    scanner.nextLine();
//    return quantity;
//  }
//
//  public Product inputProductDetails() {
//    String productCode = inputProductCode();
//    String productName = inputProductName();
//    int quantity = inputQuantity();
//    System.out.println();
//    return new Product(productCode, productName, quantity);
//  }
//}
