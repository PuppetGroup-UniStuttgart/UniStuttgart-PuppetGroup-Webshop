package cloudlab.webshop;

import io.grpc.stub.StreamObserver;

import cloudlab.webshop.Webshop.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WebshopImpl implements WebShopGrpc.WebShop {

    static Map<String, Product> products = new HashMap<String, Webshop.Product>();
    static Map<String, Order> orders = new HashMap<String, Webshop.Order>();

    static {

        Product product1 = Product.newBuilder().setId(UUID.randomUUID().toString()).setName("Nikon COOLPIX S33 Compact Digital Camera")
                .setPrice(89)
                .setProducer("Nikon")
                .setWeight(0.1f)
                .setAvailableStock(3)
                .setCategory("Camera").build();
        products.put(product1.getId(), product1);
        Product product2 = Product.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setName("Jawbone UP3 Wireless Sleep Wristband")
                .setPrice(69)
                .setProducer("Jawbone")
                .setWeight(0.001f)
                .setAvailableStock(5)
                .setCategory("Activity Trackers").build();
        products.put(product2.getId(), product2);
        Product product3 = Product.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setName("KRUPS Automatic Coffee Machine")
                .setPrice(384)
                .setProducer("Krups")
                .setWeight(5.6f)
                .setAvailableStock(5)
                .setCategory("Kitchen").build();
        products.put(product3.getId(), product3);
        Product product4 = Product.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setName("Pollini Slingback Sandal")
                .setPrice(331)
                .setProducer("Pollini")
                .setWeight(0.3f)
                .setAvailableStock(1)
                .setCategory("Shoes").build();
        products.put(product4.getId(), product4);
        Product product5 = Product.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setName("Lenovo Ideapad U330P")
                .setPrice(499)
                .setProducer("Lenovo")
                .setWeight(1.6f)
                .setAvailableStock(4)
                .setCategory("Laptop").build();
        products.put(product5.getId(), product5);
    }

    @Override
    public void listProducts(Webshop.ListProductsParams request, StreamObserver<Webshop.Product> responseObserver) {
        int resultsLimit = request.getLimit();
        int index = 0;
        for (String productId : products.keySet()) {
            if (index < resultsLimit) {
                Product product = products.get(productId);
                responseObserver.onNext(product);
                index++;
            } else {
                break;
            }
        }
        responseObserver.onCompleted();
    }

    @Override
    public void checkAvailability(ProductId request, StreamObserver<Availability> responseObserver) {
        Product requestedProduct = products.get(request.getId());
        Availability availability;
        if (requestedProduct.getAvailableStock() > 0) {
            availability = Availability.newBuilder().setAvailable(true).build();
        } else {
            availability = Availability.newBuilder().setAvailable(false).build();
        }
        responseObserver.onNext(availability);
        responseObserver.onCompleted();
    }

    @Override
    public void storeOrderDetails(Order request, StreamObserver<OrderId> responseObserver) {
        Order order = request;
        if (order.getId().equals("")) {
            order = Order.newBuilder(order).setId(UUID.randomUUID().toString()).build();
        }
        orders.put(order.getId(), order);
        responseObserver.onNext(OrderId.newBuilder().setId(order.getId()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getOrderDetails(OrderId request, StreamObserver<Order> responseObserver) {
        Order order = orders.get(request.getId());
        responseObserver.onNext(order);
        responseObserver.onCompleted();
    }

    @Override
    public void cancelOrder(OrderId request, StreamObserver<Order> responseObserver) {
        Order order = orders.get(request.getId());
        order = Order.newBuilder(order).setStatus(Order.Status.CANCELED).build();
        orders.put(order.getId(), order);
        responseObserver.onNext(order);
        responseObserver.onCompleted();
    }

    /**
     * Transaction cost is assumed to be the sum of the prices of the products part of this order
     */
    @Override
    public void calcTransactionCosts(OrderId request, StreamObserver<Costs> responseObserver) {
        Order order = orders.get(request.getId());
        List<ProductId> productsList = order.getProductsList();
        float totalCost = 0.0f;
        for (ProductId productID : productsList) {
            Product product = products.get(productID.getId());
            totalCost += product.getPrice();
        }
        responseObserver.onNext(Costs.newBuilder().setCosts(totalCost).build());
        responseObserver.onCompleted();
    }

    @Override
    public void conductPayment(Payment request, StreamObserver<Order> responseObserver) {
        Payment payment = request;
        Order order = orders.get(payment.getId().getId());
        order = Order.newBuilder(order).setStatus(Order.Status.PAYED).build();
        orders.put(order.getId(), order);
        responseObserver.onNext(order);
        responseObserver.onCompleted();
    }

    /**
     * Shipment cost is assumed to be 0.1 times the weight of the products in the order
     */
    @Override
    public void calcShipmentCosts(OrderId request, StreamObserver<Costs> responseObserver) {
        Order order = orders.get(request.getId());
        List<ProductId> productsList = order.getProductsList();
        float totalWeight = 0.0f;
        for (ProductId productID : productsList) {
            Product product = products.get(productID.getId());
            totalWeight += product.getWeight();
        }
        float shipmentCost = totalWeight * 0.1f;
        responseObserver.onNext(Costs.newBuilder().setCosts(shipmentCost).build());
        responseObserver.onCompleted();
    }

    @Override
    public void shipProducts(OrderId request, StreamObserver<Order> responseObserver) {
        Order order = orders.get(request.getId());
        order = Order.newBuilder(order).setStatus(Order.Status.SHIPPED).build();
        List<ProductId> productsList = order.getProductsList();
        for (ProductId productID : productsList) {
            Product product = products.get(productID.getId());
            int currentStock = product.getAvailableStock();
            product = Product.newBuilder(product).setAvailableStock(currentStock - 1).build();
            products.put(product.getId(), product);
        }
        responseObserver.onNext(order);
        responseObserver.onCompleted();
    }

    @Override
    public void getProductDetails(ProductId request, StreamObserver<Product> responseObserver) {
        Product product = products.get(request.getId());
        responseObserver.onNext(product);
        responseObserver.onCompleted();
    }
}
