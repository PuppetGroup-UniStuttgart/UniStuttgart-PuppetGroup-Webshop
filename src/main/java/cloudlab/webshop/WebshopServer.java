package cloudlab.webshop;

import io.grpc.ServerBuilder;

import java.util.logging.Logger;

/**
 * Created by shbe on 17.05.2016.
 */
public class WebshopServer {
    private static final Logger logger = Logger.getLogger(WebshopServer.class.getName());

    private int port = 50051;
    private io.grpc.Server server;

    private void start() throws Exception {
        server = ServerBuilder.forPort(port).addService(WebShopGrpc.bindService(new WebshopImpl())).build().start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                WebshopServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

	/* Await termination on the main thread since the grpc library uses daemon
	 threads.*/

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws Exception {
        final WebshopServer server = new WebshopServer();
        server.start();
        server.blockUntilShutdown();
    }
}
