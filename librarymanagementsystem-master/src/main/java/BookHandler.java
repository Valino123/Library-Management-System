import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sun.net.httpserver.*;

import entities.Book;
import entities.Borrow;
import entities.Book.SortColumn;
import queries.ApiResult;
import queries.BookQueryConditions;
import queries.BookQueryResults;
import queries.SortOrder;
class Filters {
    private String category;
    private String title;
    private String press;
    private String minPublishYear;
    private String maxPublishYear;
    private String author;
    private String minPrice;
    private String maxPrice;
    public String getCategory(){return this.category;}
    public String getPress(){return this.press;}
    public String getTitle(){return this.title;}
    public String getAuthor(){return this.author;}
    public String getMinPublishYear(){return this.minPublishYear;}
    public String getMaxPublishYear(){return this.maxPublishYear;}
    public String getMinPrice(){return this.minPrice;}
    public String getMaxPrice(){return this.maxPrice;}

}
public class BookHandler implements HttpHandler{
    public LibraryManagementSystemImpl api;
    public BookHandler(LibraryManagementSystemImpl api){this.api = api;}
    private Map<String, String> parseQuery(String query) throws UnsupportedEncodingException {
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length > 1) {
                    params.put(URLDecoder.decode(pair[0], "UTF-8"), URLDecoder.decode(pair[1], "UTF-8"));
                } else if (pair.length > 0) {
                    params.put(URLDecoder.decode(pair[0], "UTF-8"), "");
                }
            }
        }
        return params;
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException{
        // 允许所有域的请求，cors处理, 解决跨域问题
        Headers headers = exchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        headers.add("Access-Control-Allow-Headers", "Content-Type");
        // 解析请求的方法，看GET还是POST
        String requestMethod = exchange.getRequestMethod();
        if(requestMethod.equals("GET")){
            handleGetRequest(exchange);
        }else if(requestMethod.equals("POST")){
            handlePostRequest(exchange);
        }else if(requestMethod.equals("OPTIONS")){
            handleOptionsRequest(exchange);
        }else if(requestMethod.equals("PUT")){
            handlePutRequest(exchange);
        }else{
            exchange.sendResponseHeaders(405, -1);
        }

    }
    public void handleGetRequest(HttpExchange exchange) throws IOException{
        URI requestURI = exchange.getRequestURI();
        String query = requestURI.getQuery();
        Map<String, String> params = parseQuery(query);

        // Now you can use the params map to get your parameters
        String sortBy = params.get("selected");
        String sortOrder = params.get("order");
        String filtersJson = params.get("filters");

        // Parse the filters JSON string into a Filters object
        ObjectMapper mapper = new ObjectMapper();
        Filters filters = mapper.readValue(filtersJson, Filters.class);
        String category = filters.getCategory();
        String title = filters.getTitle();
        String press = filters.getPress();
        String author = filters.getAuthor();
        int minPublishYear = 0;
        if(!filters.getMinPublishYear().isEmpty()){
            minPublishYear = Integer.parseInt(filters.getMinPublishYear());
        }
        int maxPublishYear = 0;
        if(!filters.getMaxPublishYear().isEmpty()){
            maxPublishYear = Integer.parseInt(filters.getMaxPublishYear());
        }
        double minPrice = 0;
        if(!filters.getMinPrice().isEmpty()){
            minPrice = Double.parseDouble(filters.getMinPrice());
        }
        double maxPrice = 0;
        if(!filters.getMaxPrice().isEmpty()){
            maxPrice = Double.parseDouble(filters.getMaxPrice());
        }
        BookQueryConditions bookQueryConditions = new BookQueryConditions();
        if(!author.isEmpty())
            bookQueryConditions.setAuthor(author);
        if(!category.isEmpty())
            bookQueryConditions.setCategory(category);
        if(maxPrice != 0)
            bookQueryConditions.setMaxPrice(maxPrice);
        if(maxPublishYear != 0)
            bookQueryConditions.setMaxPublishYear(maxPublishYear);
        if(minPrice != 0)
            bookQueryConditions.setMinPrice(minPrice);
        if(minPublishYear !=0)
            bookQueryConditions.setMinPublishYear(minPublishYear);
        if(!press.isEmpty())
            bookQueryConditions.setPress(press);
        bookQueryConditions.setSortBy(SortColumn.str2Const(sortBy));
        bookQueryConditions.setSortOrder(SortOrder.str2Const(sortOrder));
        if(!title.isEmpty())
            bookQueryConditions.setTitle(title);
        // Query the database
        ApiResult ret = api.queryBook(bookQueryConditions);
        //响应头， JSON通信
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        // 状态码200 ->status ok
        exchange.sendResponseHeaders(200, 0);
        // 获取输出流
        OutputStream outputStream = exchange.getResponseBody();
        // 构建JSON响应数据， 这里简化为字符串
        //这里写一个固定的JSON，实际可以查表获取数据，然后拼出想要的JSON
        BookQueryResults bookQueryResults = (BookQueryResults)ret.payload;
        String response;
        mapper = new ObjectMapper();
        // Convert bookQueryResults to List<Book>
        List<Book> bookList = bookQueryResults.getResults();
        try{
            response = mapper.writeValueAsString(bookList);
            System.out.println(response);
            outputStream.write(response.getBytes());
        }catch(IOException e){
            e.printStackTrace();
        }
        // 流一定要close！！！小心泄漏
        outputStream.close();      
        System.out.println("exit handleGetRequest");
    }
    public void handlePostRequest(HttpExchange exchange) throws IOException{
           // show func name
           System.out.println("enter handlePostRequest");
           // read post request body
           InputStream requestBody = exchange.getRequestBody();
           // construct a buffered reader
           BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));
           // concatenate strings
           StringBuilder requestBodyBuilder = new StringBuilder();
           // container
           String line;
           // keep reading
           while ((line = reader.readLine()) != null) {
               requestBodyBuilder.append(line);
           }
           // Create a new ObjectMapper instance
           ObjectMapper mapper = new ObjectMapper();
           // Convert the JSON string to a Map
           Map<String, String> map = mapper.readValue(requestBodyBuilder.toString(), new TypeReference<Map<String, String>>(){});
           // Extract the card details from the map
           String task = map.get("task");
           ApiResult ret = null;
           if(task.equals("图书入库")){
               String category = map.get("category");
               String title = map.get("title");
               String press = map.get("press");
               int publishYear = Integer.parseInt(map.get("publishYear"));
               String author = map.get("author");
               double price = Double.parseDouble(map.get("price"));
               int stock = Integer.parseInt(map.get("stock"));
               Book book = new Book(category, title, press, publishYear, author, price, stock);
               ret = api.storeBook(book);
           }else if(task.equals("批量入库")){
               String url = map.get("url");
            //    url = "E:/Code/DBMS/library-frontend/librarymanagementsystem-master/src/main/java/bookList.csv";
               
               List<Book> books = new ArrayList<>();
               Reader in = new FileReader(url);
               Iterable<CSVRecord> records = 
                   CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
               for(CSVRecord record: records){
                   String category = record.get("category");
                   String title = record.get("title");
                   String press = record.get("press");
                   int publishYear = Integer.parseInt(record.get("publishYear"));
                   String author = record.get("author");
                   double price = Double.parseDouble(record.get("price"));
                   int stock = Integer.parseInt(record.get("stock"));
                   books.add(new Book(category, title, press, publishYear, author, price, stock));
               }
               ret = api.storeBook(books);
   
           }else if(task.equals("增加库存")){
               int bookId = Integer.parseInt(map.get("bookId")) ;
               int deltaStock = Integer.parseInt(map.get("deltaStock"));
               ret = api.incBookStock(bookId, deltaStock);
           }else if(task.equals("借阅图书")){
               int bookId = Integer.parseInt(map.get("bookId"));
               int cardId = Integer.parseInt(map.get("cardId"));
               long borrowTime = Long.parseLong(map.get("borrowTime"));
               ret = api.borrowBook(new Borrow(cardId, bookId, borrowTime, 0));
           }else{
               ret = null;
           };
           // print apiresutl return message
           System.out.println(ret.message);
           // convert the boolean to a JSON string
           String responseJson = mapper.writeValueAsString(ret.ok);
           // set the response headers
           exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
           // send the response headers
           exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, responseJson.length());
           // Get the response body output stream
           OutputStream responseBody = exchange.getResponseBody();
           // Write the response 
           responseBody.write(responseJson.getBytes(StandardCharsets.UTF_8));
           //close 
           responseBody.close();
           System.out.println("exit handlePostRequest");
    }
    public void handleOptionsRequest(HttpExchange exchange) throws IOException{
        // show func
        System.out.println("enter handleOpRequest");

        // 读取Options请求体
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
    
    public void handlePutRequest(HttpExchange exchange) throws IOException{
        // show func name
        System.out.println("enter handlePutRequest");
        // read post request body
        InputStream requestBody = exchange.getRequestBody();
        // construct a buffered reader
        BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));
        // concatenate strings
        StringBuilder requestBodyBuilder = new StringBuilder();
        // container
        String line;
        // keep reading
        while ((line = reader.readLine()) != null) {
            requestBodyBuilder.append(line);
        }
        // Create a new ObjectMapper instance
        ObjectMapper mapper = new ObjectMapper();
        // Convert the JSON string to a Map
        Map<String, String> map = mapper.readValue(requestBodyBuilder.toString(), new TypeReference<Map<String, String>>(){});
        // Extract the card details from the map
        String task = map.get("task");
        ApiResult ret = null;
        if(task.equals("归还图书")){
            int bookId = Integer.parseInt(map.get("bookId"));
            int cardId = Integer.parseInt(map.get("cardId"));
            long borrowTime = Long.parseLong(map.get("borrowTime"));
            long returnTime = Long.parseLong(map.get("returnTime"));
            Borrow borrow = new Borrow(cardId, bookId, borrowTime, returnTime);
            ret = api.returnBook(borrow);
        }else if(task.equals("修改图书信息")){
            int bookId = Integer.parseInt(map.get("bookId"));
            String category = map.get("category");
            String title = map.get("title");
            String press = map.get("press");
            int publishYear = Integer.parseInt(map.get("publishYear"));
            String author = map.get("author");
            double price = Double.parseDouble(map.get("price"));
            int stock = Integer.parseInt(map.get("stock"));
            Book book = new Book(category, title, press, publishYear, author, price, stock);
            book.setBookId(bookId);
            ret = api.modifyBookInfo(book);

        }else{
            ret = null;
        };
        // print apiresutl return message
        System.out.println(ret.message);
        // convert the boolean to a JSON string
        String responseJson = mapper.writeValueAsString(ret.ok);
        // set the response headers
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        // send the response headers
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, responseJson.length());
        // Get the response body output stream
        OutputStream responseBody = exchange.getResponseBody();
        // Write the response 
        responseBody.write(responseJson.getBytes(StandardCharsets.UTF_8));
        //close 
        responseBody.close();
        System.out.println("exit handlePostRequest");
    }
}

