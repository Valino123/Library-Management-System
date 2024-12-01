import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.sun.net.httpserver.*;

import entities.Borrow;
import queries.*;
import queries.BorrowHistories.Item;

public class BorrowHandler implements HttpHandler{
    public LibraryManagementSystemImpl api;
    public BorrowHandler(LibraryManagementSystemImpl api){this.api = api;}
    @Override
    public void handle(HttpExchange exchange) throws IOException{
        // 允许所有域的请求，cors处理, 解决跨域问题
        Headers headers = exchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        headers.add("Access-Control-Allow-Headers", "Content-Type");
        // 解析请求的方法，看GET还是POST
        String requestMethod = exchange.getRequestMethod();
        if(requestMethod.equals("GET")){
            handleGetRequest(exchange);
        }else if(requestMethod.equals("OPTIONS")){
            handleOptionsRequest(exchange);
        }else{
            exchange.sendResponseHeaders(405, -1);
        }

    }
    private void handleGetRequest(HttpExchange exchange) throws IOException{
        // print func name
        System.out.println("enter handleGetRequest");
       // Get the URI query parameters
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = queryToMap(query);

        // Get the cardID parameter
        String cardID = params.get("cardID");

        // print cardId
        System.out.println(cardID);
        //响应头， JSON通信
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        // 状态码200 ->status ok
        exchange.sendResponseHeaders(200, 0);
        // 获取输出流
        OutputStream outputStream = exchange.getResponseBody();
        // 构建JSON响应数据， 这里简化为字符串
        //这里写一个固定的JSON，实际可以查表获取数据，然后拼出想要的JSON
        BorrowHistories borrowhistories = (BorrowHistories)api.showBorrowHistory(Integer.parseInt(cardID)).payload;
        String response;
        ObjectMapper mapper = new ObjectMapper();
        // convert List<Item> to List<Borrow>
        List<Borrow> borrowList = new ArrayList<>();
        for(Item item: borrowhistories.getItems()){
            Borrow borrowItem = new Borrow(item.getCardId(), item.getBookId(), item.getBorrowTime(), item.getReturnTime());
            borrowList.add(borrowItem);
        }
        try{
            response = mapper.writeValueAsString(borrowList);
            response = response.replace("cardId", "cardID");
            response = response.replace("bookId", "bookID");

            System.out.println(response);
            outputStream.write(response.getBytes());
        }catch(IOException e){
            e.printStackTrace();
        }
        // 流一定要close！！！小心泄漏
        outputStream.close();      
        System.out.println("exit handleGetRequest");

    }
    
    /**
     * Converts a URI query string to a map of key-value pairs.
     */
    private static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }
    private void handleOptionsRequest(HttpExchange exchange) throws IOException {
        // show func
        System.out.println("enter handleOpRequest");

        // 读取POST请求体
        InputStream requestBody = exchange.getRequestBody();
        // 用这个请求体（输入流）构造个buffered reader
        BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));
        // 拼字符串的
        StringBuilder requestBodyBuilder = new StringBuilder();
        // 用来读的
        String line;
        // 没读完，一直读，拼到string builder里
        while ((line = reader.readLine()) != null) {
            requestBodyBuilder.append(line);
        }
    
        // 看看读到了啥
        // 实际处理可能会更复杂点
        System.out.println("Received OPTION request :" + requestBodyBuilder.toString());
    
        // 响应头
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        // 响应状态码200
        exchange.sendResponseHeaders(204, 0);
    
        // 剩下三个和GET一样
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write("Finish options successfully".getBytes());
        outputStream.close();

        System.out.println("exit handleOpRequest");

    }
}
