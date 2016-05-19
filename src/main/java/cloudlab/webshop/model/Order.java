package cloudlab.webshop.model;

import java.util.List;

/**
 * Created by shbe on 17.05.2016.
 */
public class Order {
    String id; // order ID; is optional for storeOrderDetails requests
    List<Product> products;
    Customer customer;
    Status status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
