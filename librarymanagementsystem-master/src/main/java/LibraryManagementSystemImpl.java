import entities.Book;
import entities.Borrow;
import entities.Card;
import queries.*;
import queries.BorrowHistories.Item;
import utils.DBInitializer;
import utils.DatabaseConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LibraryManagementSystemImpl implements LibraryManagementSystem {

    private final DatabaseConnector connector;

    public LibraryManagementSystemImpl(DatabaseConnector connector) {
        this.connector = connector;
    }

    @Override
    public ApiResult storeBook(Book book) {
        Connection conn = connector.getConn();
       
        String checkSql = "SELECT * FROM book WHERE category = ? AND title = ? AND press = ? AND publish_year = ? AND author = ? ";
        String insertSql = "INSERT INTO book(category, title, press, publish_year, author, price, stock) VALUES (?,?,?,?,?,?,?)";
        PreparedStatement pstmt = null;
        try{
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setString(1, book.getCategory());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getPress());
            pstmt.setInt(4, book.getPublishYear());
            pstmt.setString(5, book.getAuthor());

            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                conn.rollback();
                return new ApiResult(false, "The book already exists in the database");
            }

            pstmt = conn.prepareStatement(insertSql,Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, book.getCategory());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getPress());
            pstmt.setInt(4, book.getPublishYear());
            pstmt.setString(5, book.getAuthor());
            pstmt.setDouble(6, book.getPrice());
            pstmt.setInt(7, book.getStock());

            int affectedRows = pstmt.executeUpdate();

            if(affectedRows > 0){
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if(generatedKeys.next()){
                    book.setBookId(generatedKeys.getInt(1));
                }
                conn.commit();;
                return new ApiResult(true, "Book stored successfully. Book Id is: " + book.getBookId());
            }else{
                conn.rollback();
                return new ApiResult(false, "Failed to store the book");
            }
        }catch(SQLException e){
            if(conn != null){
                try{
                    conn.rollback();
                }catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return new ApiResult(false, "Error: " + e.getMessage());
        }finally{
            if(pstmt != null){
                try{
                    pstmt.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public ApiResult incBookStock(int bookId, int deltaStock) {
        Connection conn = connector.getConn();
        String checkSql = "SELECT * FROM book WHERE book_id = ?";
        String updateSql = "UPDATE book SET stock = ? WHERE book_id = ?";
        PreparedStatement checkPstmt = null;
        PreparedStatement updatePstmt = null;

        try{
            int curStock = 0;
            int updatedStock = 0;
            conn.setAutoCommit(false);
            checkPstmt = conn.prepareStatement(checkSql);
            checkPstmt.setInt(1, bookId);

            ResultSet rs = checkPstmt.executeQuery();
            if(rs.next()){
                curStock = rs.getInt("stock");
                updatedStock = curStock + deltaStock;

                if(updatedStock < 0){
                    return new ApiResult(false, "Stock can't be negative");
                }
                updatePstmt = conn.prepareStatement(updateSql);
                updatePstmt.setInt(1, updatedStock);
                updatePstmt.setInt(2, bookId);

                int affectedRows = updatePstmt.executeUpdate();
                if(affectedRows > 0){
                    conn.commit();
                    return new ApiResult(true, "Stock updated successfully");
                }else{
                    conn.rollback();
                    return new ApiResult(false, "Failed to update the stock");
                }
            }else{
                return new ApiResult(false, "Book not exists");
            }
        }catch(SQLException e){
            if(conn != null){
                try{
                    conn.rollback();
                }catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return new ApiResult(false, "Error: " + e.getMessage());
        }finally{
            if(checkPstmt != null){
                try{
                    checkPstmt.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            }
            if(updatePstmt != null){
                try{
                    updatePstmt.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public ApiResult storeBook(List<Book> books) {
        Set<Book> uniqueBooks = new HashSet<>(books);
        if(uniqueBooks.size() < books.size()){
            return new ApiResult(false, "Duplicate books");
        }

        String checkSql = "SELECT * FROM book WHERE category = ? AND title = ? AND press = ? AND publish_year = ? AND author = ? ";
        String insertSql = "INSERT INTO book(category, title, press, publish_year, author, price, stock) VALUES (?,?,?,?,?,?,?)";
        
        PreparedStatement checkPstmt = null;
        PreparedStatement insertPstmt = null;
        Connection conn = null;
        try{
            conn = connector.getConn();
            conn.setAutoCommit(false);

            checkPstmt = conn.prepareStatement(checkSql);
            insertPstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);

            for(Book book : books){
                checkPstmt.setString(1, book.getCategory());
                checkPstmt.setString(2, book.getTitle());
                checkPstmt.setString(3, book.getPress());
                checkPstmt.setInt(4, book.getPublishYear());
                checkPstmt.setString(5, book.getAuthor());

                ResultSet rs = checkPstmt.executeQuery();
                if(rs.next()){
                    conn.rollback();
                    return new ApiResult(false, "One of the books already exists in the database");
                }

                insertPstmt.setString(1, book.getCategory());
                insertPstmt.setString(2, book.getTitle());
                insertPstmt.setString(3, book.getPress());
                insertPstmt.setInt(4, book.getPublishYear());
                insertPstmt.setString(5, book.getAuthor());
                insertPstmt.setDouble(6, book.getPrice());
                insertPstmt.setInt(7, book.getStock());

                insertPstmt.addBatch();
            }

            int[] affectedRows = insertPstmt.executeBatch();

            for(int i: affectedRows){
                if(i == 0){
                    conn.rollback();
                    return new ApiResult(false, "Failed to store one of the books");
                }
            }

            ResultSet insertRs = insertPstmt.getGeneratedKeys();
            for(Book book: books){
                if(insertRs.next()){
                    book.setBookId(insertRs.getInt(1));
                }
            }

            conn.commit();
            return new ApiResult(true, "Books stored successfully");
        }catch(SQLException e){
            if(conn != null){
                try{
                    conn.rollback();
                }catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return new ApiResult(false, "Error: " + e.getMessage());
        }finally{
            if(checkPstmt != null){
                try{
                    checkPstmt.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            }
            if(insertPstmt != null){
                try{
                    insertPstmt.close();
                }catch(SQLException e){
                    e.printStackTrace();;
                }
            }
        }
    }

    @Override
    public ApiResult removeBook(int bookId) {
        Connection conn = connector.getConn();

        String checkBorrowSql = "SELECT * FROM borrow WHERE book_id = ?";
        String checkBookSql = "SELECT * FROM book WHERE book_id = ?";
        String delSql = "DELETE FROM book WHERE book_id = ?";
        PreparedStatement checkBorrowPstmt = null;
        PreparedStatement checkBookPstmt = null;
        PreparedStatement delPstmt = null;

        try{
            conn.setAutoCommit(false);
            checkBorrowPstmt = conn.prepareStatement(checkBorrowSql);
            checkBorrowPstmt.setInt(1, bookId);

            ResultSet rs = checkBorrowPstmt.executeQuery();
            while(rs.next()){
                long returnTime = rs.getLong("return_time");
                if(returnTime == 0){
                    conn.rollback();
                    return new ApiResult(false, "Book not returned");
                }
            }
            
            checkBookPstmt = conn.prepareStatement(checkBookSql);
            checkBookPstmt.setInt(1, bookId);
            rs = checkBookPstmt.executeQuery();
            if(!rs.next()){
                return new ApiResult(false, "Book not exists");
            }

            delPstmt = conn.prepareStatement(delSql);
            delPstmt.setInt(1, bookId);

            int affectedRows = delPstmt.executeUpdate();

            if(affectedRows > 0){
                conn.commit();
                return new ApiResult(true, "Book removed successfully");
            }else{
                conn.rollback();
                return new ApiResult(false, "Failed to remove book");
            }
        }catch(SQLException e){
            if(conn != null){
                try{
                    conn.rollback();
                }catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return new ApiResult(false, "Error: " + e.getMessage());
        }finally{
            if(checkBorrowPstmt != null){
                try{
                    checkBorrowPstmt.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            }
            if(checkBookPstmt != null){
                try{
                    checkBookPstmt.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            }
            if(delPstmt != null){
                try{
                    delPstmt.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public ApiResult modifyBookInfo(Book book) {
        Connection conn = connector.getConn();
        String checkSql = "SELECT * FROM book WHERE book_id = ?";
        String modifySql = "UPDATE book SET category = ?, title = ?, press = ?, publish_year = ?, author = ?, price = ? WHERE book_id = ?";
        PreparedStatement checkPstmt = null;
        PreparedStatement modifyPstmt = null;
        try{
            conn.setAutoCommit(false);
            checkPstmt = conn.prepareStatement(checkSql);
            checkPstmt.setInt(1, book.getBookId());

            ResultSet rs = checkPstmt.executeQuery();
            if(rs.next()){
                // if(rs.getInt("stock") != book.getStock()){
                //     conn.rollback();
                //     return new ApiResult(false, "Invalid modification");
                // }
                modifyPstmt = conn.prepareStatement(modifySql);
                modifyPstmt.setString(1, book.getCategory());
                modifyPstmt.setString(2, book.getTitle());
                modifyPstmt.setString(3, book.getPress());
                modifyPstmt.setInt(4, book.getPublishYear());
                modifyPstmt.setString(5, book.getAuthor());
                modifyPstmt.setDouble(6, book.getPrice());
                modifyPstmt.setInt(7, book.getBookId());

                int affectedRows = modifyPstmt.executeUpdate();

                if(affectedRows > 0){
                    conn.commit();
                    if(rs.getInt("stock") != book.getStock()){
                        return new ApiResult(true, "Modify info successfullly, stock unchanged");
                    }else{
                        return new ApiResult(true, "Modify info successfullly");
                    }
                }else{
                    conn.rollback();
                    return new ApiResult(false, "Failed to modify info");
                }
            }else{
                conn.rollback();
                return new ApiResult(false, "Book not found");
            }

        }catch(SQLException e){
            if(conn != null){
                try{
                    conn.rollback();
                }catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return new ApiResult(false, "Error: " + e.getMessage());
        }finally{
            if(checkPstmt != null){
                try{
                    checkPstmt.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            }
            if(modifyPstmt != null){
                try{
                    checkPstmt.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public ApiResult queryBook(BookQueryConditions conditions) {

        Connection conn = null;
        PreparedStatement filterStmt = null;
        StringBuilder filterSql = new StringBuilder("SELECT * FROM book WHERE 1=1");
        List<String> params = new ArrayList<>();

        if(conditions.getCategory() != null){
            filterSql.append(" AND category = ?");
            params.add(conditions.getCategory());
        }

        // if(conditions.getTitle() != null){
        //     filterSql.append("AND title LIKE ?");
        //     params.add("%" + conditions.getTitle());
        // }
        // if(conditions.getTitle() != null){
        //     filterSql.append(" AND title = ?");
        //     params.add(conditions.getTitle());
        // }
        if(conditions.getTitle() != null){
            filterSql.append(" AND title LIKE ?");
            params.add("%" +conditions.getTitle() + "%");
        }

        if (conditions.getPress() != null) {
            filterSql.append(" AND press LIKE ?");
            params.add("%" + conditions.getPress() + "%");
        }
        // if (conditions.getPress() != null) {
        //     filterSql.append(" AND press = ?");
        //     params.add(conditions.getPress());
        // }

        if (conditions.getMinPublishYear() != null) {
            filterSql.append(" AND publish_year >= ?");
            params.add(String.valueOf(conditions.getMinPublishYear()));
        }
    
        if (conditions.getMaxPublishYear() != null) {
            filterSql.append(" AND publish_year <= ?");
            params.add(String.valueOf(conditions.getMaxPublishYear()));
        }

        if (conditions.getAuthor() != null) {
            filterSql.append(" AND author LIKE ?");
            params.add("%" + conditions.getAuthor() + "%");
        }
        // if (conditions.getAuthor() != null) {
        //     filterSql.append(" AND author = ?");
        //     params.add(conditions.getAuthor());
        // }
    
        if (conditions.getMinPrice() != null) {
            filterSql.append(" AND price >= ?");
            params.add(String.valueOf(conditions.getMinPrice()));
        }
    
        if (conditions.getMaxPrice() != null) {
            filterSql.append(" AND price <= ?");
            params.add(String.valueOf(conditions.getMaxPrice()));
        }

        filterSql.append(" ORDER BY " + conditions.getSortBy().name() + " " + conditions.getSortOrder().name());
        filterSql.append(", book_id ASC");

        try{
            conn = connector.getConn();
            filterStmt = conn.prepareStatement(filterSql.toString());

            for(int i=0; i<params.size(); i++){
                filterStmt.setString(i+1, params.get(i));
            }

            ResultSet rs = filterStmt.executeQuery();

            List<Book> books = new ArrayList<>();
            while(rs.next()){
                Book book = new Book(rs.getString("category"),rs.getString("title"),rs.getString("press"), rs.getInt("publish_year"), 
                    rs.getString("author"), rs.getDouble("price"), rs.getInt("stock"));
                book.setBookId(rs.getInt("book_id"));
                books.add(book);
            }
            BookQueryResults bookQueryResults = new BookQueryResults(books);
            conn.commit();
            return new ApiResult(true, bookQueryResults);
        }catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return new ApiResult(false, "Error: " + e.getMessage());
        }finally {
            if (filterStmt != null) {
                try {
                    filterStmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public ApiResult borrowBook(Borrow borrow) {
        Connection conn = connector.getConn();
        String checkStockSql = "SELECT stock FROM book WHERE book_id = ? FOR UPDATE";
        String checkBorrowSql = "SELECT return_time FROM borrow WHERE book_id = ? AND card_id = ?";
        String checkCardSql = "SELECT * FROM card WHERE card_id = ?";
        String updateStockSql = "UPDATE book SET stock = stock - 1 WHERE book_id = ?";
        String insertBorrowSql = "INSERT INTO borrow (card_id, book_id, borrow_time, return_time) VALUES (?, ?, ?, 0)";
        PreparedStatement checkStockPstmt = null;
        PreparedStatement checkBorrowPstmt = null;
        PreparedStatement checkCardPstmt = null;
        PreparedStatement updateStockPstmt = null;
        PreparedStatement insertBorrowPstmt = null;
    
        try {
            conn.setAutoCommit(false);
    
            checkStockPstmt = conn.prepareStatement(checkStockSql);
            checkStockPstmt.setInt(1, borrow.getBookId());
            ResultSet rs = checkStockPstmt.executeQuery();
            if (!rs.next() || rs.getInt("stock") <= 0) {
                conn.rollback();
                return new ApiResult(false, "Book not exists");
            }

            checkCardPstmt = conn.prepareStatement(checkCardSql);
            checkCardPstmt.setInt(1, borrow.getCardId());
            rs = checkCardPstmt.executeQuery();
            if(!rs.next()){
                conn.rollback();
                return new ApiResult(false, "Card not exists");
            }
    
            checkBorrowPstmt = conn.prepareStatement(checkBorrowSql);
            checkBorrowPstmt.setInt(1, borrow.getBookId());
            checkBorrowPstmt.setInt(2, borrow.getCardId());
            rs = checkBorrowPstmt.executeQuery();

            while(rs.next()){
                if(rs.getLong("return_time") == 0){
                    conn.rollback();
                    return new ApiResult(false, "Book not returned");
                }
            }
            // if (rs.next() && rs.getLong("return_time") == 0) {
            //     conn.rollback();
            //     return new ApiResult(false, "Book not returned");
            // }
    
            updateStockPstmt = conn.prepareStatement(updateStockSql);
            updateStockPstmt.setInt(1, borrow.getBookId());
            int affectedRows = updateStockPstmt.executeUpdate();
            if (affectedRows == 0) {
                conn.rollback();
                return new ApiResult(false, "Failed to update the stock of the book");
            }
    
            insertBorrowPstmt = conn.prepareStatement(insertBorrowSql);
            insertBorrowPstmt.setInt(1, borrow.getCardId());
            insertBorrowPstmt.setInt(2, borrow.getBookId());
            insertBorrowPstmt.setLong(3, borrow.getBorrowTime());
            affectedRows = insertBorrowPstmt.executeUpdate();
            if (affectedRows == 0) {
                conn.rollback();
                return new ApiResult(false, "Failed to add borrow record");
            }
    
            conn.commit();
            return new ApiResult(true, "Borrow operation successfully");
    
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return new ApiResult(false, "Error: " + e.getMessage());
        } finally {
            if (checkBorrowPstmt != null) {
                try {
                    checkBorrowPstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (checkStockPstmt != null) {
                try {
                    checkStockPstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (updateStockPstmt != null) {
                try {
                    updateStockPstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (insertBorrowPstmt != null) {
                try {
                    insertBorrowPstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (checkCardPstmt != null) {
                try {
                    checkCardPstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            
        }
    }

    @Override
    public ApiResult returnBook(Borrow borrow) {
        Connection conn = connector.getConn();
        String checkSql = "SELECT * FROM borrow WHERE book_id = ? AND card_id = ? AND return_time = 0";
        String updateSql = "UPDATE borrow SET return_time = ? WHERE book_id = ? AND card_id = ? AND borrow_time = ?";
        String updateStockSql = "UPDATE book SET stock = stock+1 WHERE book_id = ?";
        PreparedStatement checkPstmt = null;
        PreparedStatement updatePstmt = null;
        PreparedStatement updateStockPstmt = null;

        try{
            conn.setAutoCommit(false);
            checkPstmt = conn.prepareStatement(checkSql);
            checkPstmt.setInt(1, borrow.getBookId());
            checkPstmt.setInt(2, borrow.getCardId());

            ResultSet rs = checkPstmt.executeQuery();
            if(rs.next()){
                updatePstmt = conn.prepareStatement(updateSql);
                updatePstmt.setLong(1, borrow.getReturnTime());
                updatePstmt.setInt(2, borrow.getBookId());
                updatePstmt.setInt(3, borrow.getCardId());
                updatePstmt.setLong(4, borrow.getBorrowTime());

                int affectedRows = updatePstmt.executeUpdate();

                updateStockPstmt = conn.prepareStatement(updateStockSql);
                updateStockPstmt.setInt(1, borrow.getBookId());

                int stockAffectedRows = updateStockPstmt.executeUpdate();

                if(affectedRows > 0 && stockAffectedRows > 0){
                    conn.commit();
                    return new ApiResult(true, "Return time updated successfully");
                }else{
                    conn.rollback();
                    return new ApiResult(false, "Failed to update the return time");
                }
            }else{
                return new ApiResult(false, "Borrow record not exists");
            }
        }catch(SQLException e){
            if(conn != null){
                try{
                    conn.rollback();
                }catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return new ApiResult(false, "Error: " + e.getMessage());
        }finally{
            if(checkPstmt != null){
                try{
                    checkPstmt.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            }
            if(updatePstmt != null){
                try{
                    updatePstmt.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            }
            if(updateStockPstmt != null){
                try{
                    updatePstmt.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public ApiResult showBorrowHistory(int cardId) {
        Connection conn = connector.getConn();
        String checkSql = "SELECT * FROM borrow WHERE card_id = ? ORDER BY borrow_time DESC, book_id ASC";
        String checkBookSql = "SELECT * FROM book WHERE book_id = ?";
        PreparedStatement checkPstmt = null;
        PreparedStatement checkBookPstmt = null;
        try{
            conn.setAutoCommit(false);
            checkPstmt = conn.prepareStatement(checkSql);
            checkPstmt.setInt(1, cardId);

            ResultSet rs = checkPstmt.executeQuery();

            List<Item> items = new ArrayList<>();

            while(rs.next()){
                Item item = new Item();
                int bookId = rs.getInt("book_id");
                item.setBookId(bookId); item.setCardId(cardId);
                item.setBorrowTime(rs.getLong("borrow_time"));
                item.setReturnTime(rs.getLong("return_time"));
                try{
                    checkBookPstmt = conn.prepareStatement(checkBookSql);
                    checkBookPstmt.setInt(1, bookId);

                    ResultSet bookRs = checkBookPstmt.executeQuery();
                    if(bookRs.next()){
                        item.setAuthor(bookRs.getString("author"));
                        item.setCategory(bookRs.getString("category"));
                        item.setPress(bookRs.getString("press"));
                        item.setPrice(bookRs.getDouble("price"));
                        item.setPublishYear(bookRs.getInt("publish_year"));
                        item.setTitle(bookRs.getString("title"));
                    }else{
                        conn.rollback();
                        return new ApiResult(false, "Borrow record incomplete");
                    }
                }catch(SQLException e){
                    if(conn != null){
                        try{
                            conn.rollback();
                            return new ApiResult(false, "Failed to get info about book");
                        }catch(SQLException ex){
                            ex.printStackTrace();
                        }
                    }
                }finally{
                    if (checkBookPstmt != null) {
                        try {
                            checkBookPstmt.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }

                items.add(item);
            }

            BorrowHistories borrowHistories = new BorrowHistories(items);
            conn.commit();
            return new ApiResult(true, "Query successfully", borrowHistories);
            
        }catch(SQLException e){
            if(conn != null){
                try{
                    conn.rollback();
                }catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
            System.out.println("Error: " + e.getMessage());
            return new ApiResult(false, "Failed to get info about book");
        }finally{
            if (checkPstmt != null) {
                try {
                    checkPstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (checkBookPstmt != null) {
                try {
                    checkBookPstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public ApiResult registerCard(Card card) {
        Connection conn = connector.getConn();
        System.out.println("in register card");
        try {
            System.out.println("Connection is " + (conn != null && !conn.isClosed() ? "open" : "closed"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        String checkSql = "SELECT * FROM card WHERE name = ? AND department = ? AND type = ?";
        String insertSql = "INSERT INTO card(name, department, type) VALUES (?,?,?)";
        PreparedStatement checkPstmt = null;
        PreparedStatement insertPstmt = null;

        try{
            System.out.println("in register card--try");
            conn.setAutoCommit(false);
            checkPstmt = conn.prepareStatement(checkSql);
            checkPstmt.setString(1, card.getName());
            checkPstmt.setString(2, card.getDepartment());
            checkPstmt.setString(3, card.getType().getStr());
            System.out.println("in register card--810");
            ResultSet rs = checkPstmt.executeQuery();
            System.out.println("in register card--806");

            if(rs.next()){
                conn.rollback();
                return new ApiResult(false, "The card already exists in the database");

            }
            System.out.println("in register card--811");
            insertPstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            insertPstmt.setString(1, card.getName());
            insertPstmt.setString(2, card.getDepartment());
            insertPstmt.setString(3, card.getType().getStr());

            int affectedRows = insertPstmt.executeUpdate();

            if(affectedRows > 0){
                System.out.println("in register card--820");
                ResultSet generatedKeys = insertPstmt.getGeneratedKeys();
                if(generatedKeys.next()){
                    card.setCardId(generatedKeys.getInt(1));
                }
                conn.commit();;
                System.out.println("in register card--826");
                return new ApiResult(true, "Card registered successfully. Card Id is: " + card.getCardId());
            }else{
                conn.rollback();
                return new ApiResult(false, "Failed to register the card");
            }
        }catch(SQLException e){
            if(conn != null){
                try{
                    conn.rollback();
                }catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return new ApiResult(false, "Error: " + e.getMessage());
        }finally{
            if(checkPstmt != null){
                try{
                    checkPstmt.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            }
            if(insertPstmt != null){
                try{
                    insertPstmt.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    public ApiResult modifyCardInfo(Card card){
        Connection conn = connector.getConn();
        String checkSql = "SELECT * FROM card WHERE card_id = ?";
        String checkBorrowSql = "SELECT * FROM borrow WHERE card_id = ?";
        String modifySql = "UPDATE card SET name = ?, department = ?, type = ? WHERE card_id = ?";
        PreparedStatement checkBorrowPstmt = null;
        PreparedStatement checkPstmt = null;
        PreparedStatement modifyPstmt = null;
        try{
            conn.setAutoCommit(false);
            checkPstmt = conn.prepareStatement(checkSql);
            checkPstmt.setInt(1, card.getCardId());

            ResultSet rs = checkPstmt.executeQuery();
            if(rs.next()){
                checkBorrowPstmt = conn.prepareStatement(checkBorrowSql);
                checkBorrowPstmt.setInt(1, card.getCardId());
                ResultSet checkBorrowRs = checkBorrowPstmt.executeQuery();
                while(checkBorrowRs.next()){
                    long returnTime = rs.getLong("return_time");
                    if(returnTime == 0){
                        conn.rollback();
                        return new ApiResult(false, "Book not returned");
                    }
                }
                String type = card.getType() == Card.CardType.Student ? "S" : "T";
                modifyPstmt = conn.prepareStatement(modifySql);
                modifyPstmt.setString(1, card.getName());
                modifyPstmt.setString(2, card.getDepartment());
                modifyPstmt.setString(3, type);
                modifyPstmt.setInt(4, card.getCardId());

                int affectedRows = modifyPstmt.executeUpdate();

                if(affectedRows > 0){
                    conn.commit();
                    return new ApiResult(true, "Card info modified successfully!");

                }else{
                    conn.rollback();
                    return new ApiResult(false, "Failed to modify Card info");
                }
            }else{
                conn.rollback();
                return new ApiResult(false, "Card not found");
            }

        }catch(SQLException e){
            if(conn != null){
                try{
                    conn.rollback();
                }catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return new ApiResult(false, "Error: " + e.getMessage());
        }finally{
            if(checkPstmt != null){
                try{
                    checkPstmt.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            }
            if(modifyPstmt != null){
                try{
                    checkPstmt.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public ApiResult removeCard(int cardId) {
        Connection conn = connector.getConn();

        String checkSql = "SELECT * FROM borrow WHERE card_id = ?";
        String delSql = "DELETE FROM card WHERE card_id = ?";
        PreparedStatement checkPstmt = null;
        PreparedStatement delPstmt = null;

        try{
            conn.setAutoCommit(false);
            checkPstmt = conn.prepareStatement(checkSql);
            checkPstmt.setInt(1, cardId);

            ResultSet rs = checkPstmt.executeQuery();
            while(rs.next()){
                long returnTime = rs.getLong("return_time");
                if(returnTime == 0){
                    conn.rollback();
                    return new ApiResult(false, "Book not returned");
                }
            }
            // if(!rs.next()){
            //     conn.rollback();
            //     return new ApiResult(false, "Card not exists in borrow histories");
            // }
            // long returnTime = rs.getLong("return_time");
            // if(returnTime == 0){
            //     conn.rollback();
            //     return new ApiResult(false, "One of the books borrowed with this card hasn't been returned");
            // }

            delPstmt = conn.prepareStatement(delSql);
            delPstmt.setInt(1, cardId);

            int affectedRows = delPstmt.executeUpdate();

            if(affectedRows > 0){
                conn.commit();
                return new ApiResult(true, "Card removed successfully");
            }else{
                conn.rollback();
                return new ApiResult(false, "Failed to remove card");
            }
        }catch(SQLException e){
            if(conn != null){
                try{
                    conn.rollback();
                }catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return new ApiResult(false, "Error: " + e.getMessage());
        }finally{
            if(checkPstmt != null){
                try{
                    checkPstmt.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            }
            if(delPstmt != null){
                try{
                    delPstmt.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public ApiResult showCards() {
        String checkSql = "SELECT * FROM card ORDER BY card_ID";
        Connection conn = connector.getConn();
        PreparedStatement checkStmt = null;
        try{
            conn.setAutoCommit(false);
            checkStmt = conn.prepareStatement(checkSql);

            ResultSet rs = checkStmt.executeQuery();

            List<Card> cards= new ArrayList<>();

            while (rs.next()) {
                Card card = new Card();
                card.setCardId(rs.getInt("card_id"));
                card.setDepartment(rs.getString("department"));
                card.setName(rs.getString("name"));
                card.setType(Card.CardType.values(rs.getString("type")));
                cards.add(card);
            }

            CardList cardlist = new CardList(cards);
            conn.commit();
            return new ApiResult(true, "Query successfully", cardlist);
        }catch(SQLException e){
            if(conn != null){
                try{
                    conn.rollback();
                }catch(SQLException ex){
                    ex.printStackTrace();
                }
               
            }
            e.printStackTrace();
            return new ApiResult(false, "Error: " + e.getMessage());
        }finally{
            if(checkStmt != null){
                try{
                    checkStmt.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public ApiResult resetDatabase() {
        Connection conn = connector.getConn();
        try {
            Statement stmt = conn.createStatement();
            DBInitializer initializer = connector.getConf().getType().getDbInitializer();
            stmt.addBatch(initializer.sqlDropBorrow());
            stmt.addBatch(initializer.sqlDropBook());
            stmt.addBatch(initializer.sqlDropCard());
            stmt.addBatch(initializer.sqlCreateCard());
            stmt.addBatch(initializer.sqlCreateBook());
            stmt.addBatch(initializer.sqlCreateBorrow());
            stmt.executeBatch();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void commit(Connection conn) {
        try {
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
