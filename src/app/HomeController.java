package app;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;
import java.util.Properties;

public class HomeController {
    @FXML
    VBox booksContainer;
    static Connection con;
    @FXML
    Label totalBooks;
    TextField[] textFields = new TextField[5];
    Statement statement;
    int totalBookCount = 0;

    public void initialize() throws IOException, SQLException {

        con = getCon();
        statement = con.createStatement();
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS Book (id SERIAL PRIMARY KEY, name CHARACTER VARYING(50) NOT NULL , description CHARACTER VARYING (300), genre CHARACTER VARYING (30) NOT NULL, rack CHARACTER VARYING (10) NOT NULL, cover_url CHARACTER VARYING (200));");

        addBooksFromDB();
        totalBooks.setText("Total Books: " + totalBookCount);
        statement.close();
    }

    Connection getCon() throws SQLException {
        String url = "jdbc:postgresql://localhost/librarymanager";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        props.setProperty("password", "yash");
        return DriverManager.getConnection(url, props);
    }

    void addBooksFromDB() throws SQLException, IOException {
        ResultSet rs = statement.executeQuery("SELECT * FROM Book");
        while (rs.next()) {
            totalBookCount++;
            Book book = new Book();
            book.setData(rs.getString("cover_url"), rs.getString("name"), rs.getString("id"), rs.getString("description"), rs.getString("genre"), rs.getString("rack"));
            book.setParentController(this);
            booksContainer.getChildren().add((Node) book.getBook());
        }

        rs.close();
    }

    @FXML
    void onAddPress() throws IOException {
        Dialog<Boolean> dialog = new Dialog<>();

        dialog.setTitle("Add Book");
        dialog.setHeaderText("Fill in book details");
        ImageView imgView = new ImageView();
        Image img = new Image(getClass().getResource(
                "../resources/book-icon.png").toString(),
                50, 50, false, false);

        imgView.setImage(img);
        dialog.setGraphic(imgView);

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 30));

        TextField bookName = new TextField();
        bookName.setPromptText("Name");
        TextField description = new TextField();
        description.setPromptText("Description");
        TextField coverURL = new TextField();
        coverURL.setPromptText("Cover URL");
        TextField genre = new TextField();
        genre.setPromptText("Genre");
        TextField rack = new TextField();
        rack.setPromptText("Rack");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(bookName, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(description, 1, 1);
        grid.add(new Label("Cover URL:"), 0, 2);
        grid.add(coverURL, 1, 2);
        grid.add(new Label("Genre:"), 0, 3);
        grid.add(genre, 1, 3);
        grid.add(new Label("Rack:"), 0, 4);
        grid.add(rack, 1, 4);


        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);

        textFields[0] = bookName;
        textFields[1] = description;
        textFields[2] = coverURL;
        textFields[3] = genre;
        textFields[4] = rack;

        bookName.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.trim().isEmpty()) {
                addButton.setDisable(fieldsEmpty(bookName));

            }
        });

        addNonEmptyCheck(bookName, addButton);
        addNonEmptyCheck(description, addButton);
        addNonEmptyCheck(coverURL, addButton);
        addNonEmptyCheck(genre, addButton);
        addNonEmptyCheck(rack, addButton);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> dialogButton == addButtonType);

        Optional<Boolean> result = dialog.showAndWait();

        Platform.runLater(bookName::requestFocus);


        if (result.isPresent()) {
            if (result.get()) {
                Book newBook = new Book();
                newBook.setParentController(this);
                try {
                    PreparedStatement st = con.prepareStatement("INSERT INTO Book (name, description, genre, rack, cover_url) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                    st.setString(1, bookName.getText());
                    st.setString(2, description.getText());
                    st.setString(3, genre.getText());
                    st.setString(4, rack.getText());
                    st.setString(5, coverURL.getText());

                    st.executeUpdate();

                    ResultSet rs = st.getGeneratedKeys();
                    int bookID = -1;
                    if (rs != null && rs.next()) {
                        bookID = rs.getInt(1);
                    } else {
                        System.out.println("ERROR");
                    }

                    newBook.setData(coverURL.getText(), bookName.getText(), Integer.toString(bookID), description.getText(), genre.getText(), rack.getText());
                    booksContainer.getChildren().add(0, (Node) newBook.getBook());
                    totalBookCount++;
                    st.close();
                    totalBooks.setText("Total Books: " + totalBookCount);
                    booksContainer.requestLayout();
                } catch (IllegalArgumentException | SQLException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Could not add book");
                    alert.setContentText("There was an error adding a book!");

                    alert.showAndWait();
                }
            }
        }
    }

    void removeBook(Node bookNode, int bookId) throws SQLException {
        try {
            PreparedStatement statement = con.prepareStatement("DELETE FROM Book WHERE id=?");
            statement.setInt(1, bookId);
            statement.executeUpdate();
            booksContainer.getChildren().remove(bookNode);
            statement.close();
            totalBookCount--;
            totalBooks.setText("Total Books: " + totalBookCount);
            booksContainer.requestLayout();
        } catch (NullPointerException e) {
            System.out.println("ERROR");
        }
    }

    boolean fieldsEmpty(TextField other) {
        for (TextField field : textFields) {
            if (!field.equals(other)) {
                if (field.getText().trim().isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    void addNonEmptyCheck(TextField obj, Node button) {
        obj.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.trim().isEmpty()) {
                button.setDisable(fieldsEmpty(obj));
            }
        });
    }

    void cleanUp() throws SQLException {
        statement.close();
        con.close();
    }
}
