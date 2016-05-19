package cloudlab.webshop;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import cloudlab.webshop.Webshop.*;

/**
 * Created by shbe on 17.05.2016.
 */
public class WebshopClient {

    private final ManagedChannel channel;
    private final WebShopGrpc.WebShopBlockingStub blockingStub;

    static Scanner in = new Scanner(System.in);

    public WebshopClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build();
        blockingStub = WebShopGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws Exception {
        WebshopClient client = new WebshopClient("localhost", 50051);
        int choice;
        boolean quit = false;
        System.out.println("Welcome to the IAAS Webshop!!! What would you like to do?");

        do {
            System.out.println("1. List Products");
            System.out.println("2. Check Product Availability");
            System.out.println("3. Store Order Details");
            System.out.println("4. Get Order Details");
            System.out.println("5. Cancel Order");
            System.out.println("6. Calculate Transaction Costs");
            System.out.println("7. Conduct Payment");
            System.out.println("8. Calculate Shipment Costs");
            System.out.println("9. Ship Products");
            System.out.println("10. Exit");
            System.out.println("Enter Choice: ");
            choice = in.nextInt();

            switch (choice) {
                case 1:
                    client.listProducts();
                    break;
                case 2:
                    client.checkProductAvailability();
                    break;
                case 3:
                    client.storeOrderDetails();
                    break;
                case 4:
                    client.getOrderDetails();
                    break;
                case 5:
                    client.cancelOrder();
                    break;
                case 6:
                    client.calcTransactionCosts();
                    break;
                case 7:
                    client.conductPayment();
                    break;
                case 8:
                    client.calcShipmentCosts();
                    break;
                case 9:
                    client.shipProducts();
                    break;
                case 10:
                    quit = true;
                    in.close();
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        } while (!quit);
        System.out.println("Bye-bye!");
    }

    private void shipProducts() {
        System.out.println("Enter order ID: ");
        String orderID = in.next();
        blockingStub.shipProducts(OrderId.newBuilder().setId(orderID).build());
        System.out.println("Order shipped");
    }

    private void calcShipmentCosts() {
        System.out.println("Enter order ID: ");
        String orderID = in.next();
        Costs costs = blockingStub.calcShipmentCosts(OrderId.newBuilder().setId(orderID).build());
        System.out.println("Shipment Cost: " + costs.getCosts());
    }

    private void conductPayment() {
        System.out.println("Enter order ID to process payment for: ");
        String orderID = in.next();
        System.out.println("Enter order amount: ");
        float amount = in.nextFloat();
        blockingStub.conductPayment(Payment.newBuilder().setId(OrderId.newBuilder().setId(orderID).build()).setAmount(amount).build());
        System.out.println("Payment Processed");
    }

    private void calcTransactionCosts() {
        System.out.println("Enter Order ID: ");
        String orderID = in.next();
        Costs costs = blockingStub.calcTransactionCosts(OrderId.newBuilder().setId(orderID).build());
        System.out.println("Transaction cost: " + costs.getCosts());
    }

    private void cancelOrder() {
        System.out.println("Enter OrderID to cancel: ");
        String orderID = in.next();
        blockingStub.cancelOrder(OrderId.newBuilder().setId(orderID).build());
        System.out.println("Order cancelled");
    }

    private void getOrderDetails() {
        System.out.println("Enter Order ID: ");
        String orderID = in.next();
        Order order = blockingStub.getOrderDetails(OrderId.newBuilder().setId(orderID).build());
        System.out.println("OrderId: " + order.getId());
        String customerName = order.getCustomer().getFirstname();
        System.out.println("Ordered by: " + customerName);
        String orderStatus = order.getStatus().name();
        System.out.println("Order status: " + orderStatus);
        StringBuilder productNames = new StringBuilder();
        List<ProductId> productsList = order.getProductsList();
        for (ProductId productID : productsList) {
            Product product = blockingStub.getProductDetails(productID);
            productNames.append(" ").append(product.getName()).append(",");
        }
        productNames.deleteCharAt(productNames.length() - 1);
        System.out.println("Products part of this order: " + productNames.toString());
    }

    private void storeOrderDetails() {
        System.out.println("Enter Customer's First Name: ");
        String firstName = in.next();
        System.out.println("Enter Customer's Last Name: ");
        String lastName = in.next();
        System.out.println("Enter Customer's Payment Details: ");
        String paymentDetails = in.next();
        System.out.println("Enter Customer's Shipping Address: ");
        String shippingAddress = in.next();
        Customer customer = Customer.newBuilder().setFirstname(firstName).setLastname(lastName).setPaymentDetails(paymentDetails).setShippingAddress(shippingAddress).build();
        Order.Builder orderBuilder = Order.newBuilder().setStatus(Order.Status.NEW).setCustomer(customer);
        System.out.println("Enter product IDs to add to this order, separated by commas");
        String productIDs = in.next();
        for (String productID : productIDs.split(",")) {
            orderBuilder.addProducts(ProductId.newBuilder().setId(productID));
        }
        Order order = orderBuilder.build();
        OrderId orderID = blockingStub.storeOrderDetails(order);
        System.out.println("Order created! Order ID is: " + orderID.getId());
    }

    private void checkProductAvailability() {
        System.out.println("Enter Product ID: ");
        String productID = in.next();
        Availability availability = blockingStub.checkAvailability(ProductId.newBuilder().setId(productID).build());
        if (availability.getAvailable()) {
            System.out.println("Product Available! You can go ahead with the order");
        } else if (!availability.getAvailable()) {
            System.out.println("Sorry! Product unavailable");
        } else {
            System.out.println("Availability unknown. Please try again");
        }
    }

    private void listProducts() {
        System.out.println("Enter the number of products to list: ");
        int limit = in.nextInt();
        Iterator<Product> productsList = blockingStub.listProducts(ListProductsParams.newBuilder().setLimit(limit).build());
        System.out.format("%s%45s%50s%20s%15s%8s%20s", "ID", "Name", "Category", "Producer", "Weight", "Price", "Available Stock");
        System.out.println();
        while (productsList.hasNext()) {
            Product product = productsList.next();
            System.out.format("%s%45s%20s%20s%10.2f%10.2f%15d", product.getId(), product.getName(), product.getCategory(), product.getProducer(), product.getWeight(), product.getPrice(), product.getAvailableStock());
            System.out.println();
        }
    }


}
