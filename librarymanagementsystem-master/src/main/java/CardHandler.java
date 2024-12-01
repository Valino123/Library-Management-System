import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sun.net.httpserver.*;


import entities.*;
import entities.Card.CardType;
import queries.*;
public class CardHandler implements HttpHandler {
    public LibraryManagementSystemImpl api;
    public CardHandler(LibraryManagementSystemImpl api){this.api = api;}
    @Override
    public void handle(HttpExchange exchange) throws IOException{
        // 允许所有域的请求，cors处理, 解决跨域问题
        Headers headers = exchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "GET, POST, DELETE,PUT, OPTIONS"); // !!!!!!!!!!!!!
        headers.add("Access-Control-Allow-Headers", "Content-Type");
        // 解析请求的方法，看GET还是POST
        String requestMethod = exchange.getRequestMethod();
        if(requestMethod.equals("GET")){
            handleGetRequest(exchange);
        }else if(requestMethod.equals("POST")){
            handlePostRequest(exchange);
        }else if(requestMethod.equals("OPTIONS")){
            handleOptionsRequest(exchange);
        }else if(requestMethod.equals("DELETE")){
            handleDeleteRequest(exchange);
        }else if(requestMethod.equals("PUT")){
            handlePutRequest(exchange);
        }else{
            exchange.sendResponseHeaders(405, -1);
        }

    }
    public void handlePutRequest(HttpExchange exchange) throws IOException{
        System.out.println("enter handlePutRequest");
        // Get the request body 
        InputStream requestBody = exchange.getRequestBody();
        // create objectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();
        // read request body to map
        Map<String, String> params = objectMapper.readValue(requestBody, new TypeReference<Map<String,String>>(){});
        // get the card parameters and construct a card
        String id = params.get("id");
        String name = params.get("name");
        String department = params.get("department");
        String type = params.get("type");
        Card card = new Card(Integer.parseInt(id), name, department, CardType.values(type));
        // modify using api to database
        Boolean ret = api.modifyCardInfo(card).ok;
        // convert the boolean to a JSON string
        String responseJson = objectMapper.writeValueAsString(ret);
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
        System.out.println("exit handlePutRequest");
    }
    private void handleDeleteRequest(HttpExchange exchange) throws IOException{
        System.out.println("enter handleDeleteRequest");
        // Get the request body 
        InputStream requestBody = exchange.getRequestBody();
        // create objectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();
        // read request body to map
        Map<String, String> params = objectMapper.readValue(requestBody, new TypeReference<Map<String,String>>(){});
        // get the cardID parameter
        String cardID = params.get("id");
        Boolean ret = api.removeCard(Integer.parseInt(cardID)).ok;
        // convert the boolean to a JSON string
        String responseJson = objectMapper.writeValueAsString(ret);
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
        System.out.println("exit handleDeleteRequest");
    }
    private void handleGetRequest(HttpExchange exchange) throws IOException{
        // print func name
        System.out.println("enter handleGetRequest");

        //响应头， JSON通信
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        // 状态码200 ->status ok
        exchange.sendResponseHeaders(200, 0);
        // 获取输出流
        OutputStream outputStream = exchange.getResponseBody();
        // 构建JSON响应数据， 这里简化为字符串
        //这里写一个固定的JSON，实际可以查表获取数据，然后拼出想要的JSON
        CardList cardList = (CardList)api.showCards().payload;
        String response;
        ObjectMapper mapper = new ObjectMapper();
        try{
            response = mapper.writeValueAsString(cardList.getCards());
            response = response.replace("cardId", "id");
            outputStream.write(response.getBytes());
            System.out.println(response);
        }catch(IOException e){
            e.printStackTrace();
        }
    
        // response = "[{\"id\": 1, \"name\": \"John Doe\", \"department\": \"Computer Science\", \"type\": \"Student\"}," +
        //         "{\"id\": 2, \"name\": \"Jane Smith\", \"department\": \"Electrical Engineering\", \"type\": \"Faculty\"}]";
        // 写
        // outputStream.write(response.getBytes());
        // 流一定要close！！！小心泄漏
        outputStream.close();      
        System.out.println("exit handleGetRequest");

    }
    private void handlePostRequest(HttpExchange exchange) throws IOException {
        // show func name
        System.out.println("enter handlePostRequest");
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
        // Create a new ObjectMapper instance
        ObjectMapper mapper = new ObjectMapper();

        // Convert the JSON string to a Map
        Map<String, String> map = mapper.readValue(requestBodyBuilder.toString(), new TypeReference<Map<String, String>>(){});

        // Extract the card details from the map
        int id = 0;
        String name = map.get("name");
        String department = map.get("department");
        String type = map.get("type");
        type = type.equals("学生") ? "S" : "T";
        CardType cardType = CardType.values(type);
        // print details to examine
        System.out.println(name + " " + department + " " + type);
        // Create a new card with the extracted details
        Card newCard = new Card(id, name, department, cardType);
        System.out.println("create new card instance");
        // Push to database and get cardId
        ApiResult ret = api.registerCard(newCard);
        System.out.println("push new card to database");
        // Update id if push successfully
        if(ret.ok){
            id = (int)ret.payload;
        }
        System.out.println(ret.message);

        // 实际处理可能会更复杂点
        System.out.println("Received POST request to create card with data: " + requestBodyBuilder.toString());

        // 响应头
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        // 响应状态码200
        exchange.sendResponseHeaders(200, 0);

        // 剩下三个和GET一样
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write("Card created successfully".getBytes());
        outputStream.close();
        System.out.println("exit handlePostRequest");

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

// 清理输出目录并编译项目主代码
// `mvn clean compile`

// 运行主代码
// `mvn exec:java "-Dexec.mainClass=Main" "-Dexec.cleanupDaemonThreads=false"`