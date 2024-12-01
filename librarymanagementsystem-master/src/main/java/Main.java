import utils.ConnectConfig;
import utils.DatabaseConnector;
import com.sun.net.httpserver.*;

// import entities.Card;
// import queries.CardList;

// import java.io.BufferedReader;
// import java.io.IOException;
// import java.io.InputStream;
// import java.io.InputStreamReader;
// import java.io.OutputStream;
import java.net.InetSocketAddress;
// import java.util.Map;
import java.util.logging.Logger;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.core.type.TypeReference;


public class Main {

    private static final Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            // parse connection config from "resources/application.yaml"
            ConnectConfig conf = new ConnectConfig();
            log.info("Success to parse connect config. " + conf.toString());
            // connect to database
            DatabaseConnector connector = new DatabaseConnector(conf);
            boolean connStatus = connector.connect();
            if (!connStatus) {
                log.severe("Failed to connect database.");
                System.exit(1);
            }
            /* do somethings */
            // create library interfaces instance
            LibraryManagementSystemImpl libImpl = new LibraryManagementSystemImpl(connector);
            // basic block
            // create HTTP server, listen to the port 8000
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
            //add handler, here bound to /card router
            server.createContext("/card", new CardHandler(libImpl));
            server.createContext("/borrow", new BorrowHandler(libImpl));
            server.createContext("/book", new BookHandler(libImpl));

            // start server
            server.start();
            // mark
            System.out.println("Server is listening on port 8000");
            // keep the server running until it is no longer needed
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                // release database connection handler
                if (connector.release()) {
                    log.info("Success to release connection.");
                } else {
                    log.warning("Failed to release connection.");
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
