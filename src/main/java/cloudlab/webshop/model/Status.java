package cloudlab.webshop.model;

/**
 * Created by shbe on 17.05.2016.
 */
public enum Status {
    NEW(1), PAYED(2), SHIPPED(3), CANCELED(4);

    private final int code;

    Status(int i) {
        this.code = i;
    }

    public int getCode() {
        return code;
    }
}
