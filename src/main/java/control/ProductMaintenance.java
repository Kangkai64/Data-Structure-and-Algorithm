//package control;
//
//import adt.*;
//import boundary.ProductMaintenanceUI;
//import entity.*;
//import utility.MessageUI;
//
///**
// *
// * @author Kat Tan
// */
//public class ProductMaintenance {
//
//  private ListInterface<Product> productList = new ArrayList<>();
//  private ProductDAO productDAO = new ProductDAO();
//  private ProductMaintenanceUI productUI = new ProductMaintenanceUI();
//
//  public ProductMaintenance() {
//    productList = productDAO.retrieveFromFile();
//  }
//
//   public void runProductMaintenance() {
//    int choice = 0;
//    do {
//      choice = productUI.getMenuChoice();
//      switch(choice) {
//        case 0:
//          MessageUI.displayExitMessage();
//          break;
//        case 1:
//          addNewProduct();
//          productUI.listAllProducts(getAllProducts());
//          break;
//        case 2:
//          productUI.listAllProducts(getAllProducts());
//          break;
//        default:
//          MessageUI.displayInvalidChoiceMessage();
//      }
//    } while (choice != 0);
//  }
//
//  public void addNewProduct() {
//    Product newProduct = productUI.inputProductDetails();
//    productList.add(newProduct);
//    productDAO.saveToFile(productList);
//  }
//
//  public String getAllProducts() {
//    String outputStr = "";
//    for (int i = 1; i <= productList.getNumberOfEntries(); i++) {
//      outputStr += productList.getEntry(i) + "\n";
//    }
//    return outputStr;
//  }
//
//  public void displayProducts() {
//    productUI.listAllProducts(getAllProducts());
//  }
//
//  public static void main(String[] args) {
//    ProductMaintenance productMaintenance = new ProductMaintenance();
//    productMaintenance.runProductMaintenance();
//  }
//}
