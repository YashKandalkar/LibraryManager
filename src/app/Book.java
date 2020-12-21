package app;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import java.io.IOException;
import java.sql.SQLException;

public class Book {
    @FXML
    public Text bookDescription;
    @FXML
    public Text bookID;
    @FXML
    public Text bookGenre;
    @FXML
    public Text bookRack;
    Object root;
    HomeController parentController;
    @FXML
    Label bookName;
    @FXML
    ImageView imageView;

    Book() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setController(this);
        fxmlLoader.setLocation(getClass().getResource("../resources/Book.fxml"));
        root = fxmlLoader.load();
    }

    void setData(String coverImage, String name, String id, String description, String genre, String rack) {
        bookName.setText(name);
        bookDescription.setText(description);
        bookID.setText(id);
        bookGenre.setText(genre);
        bookRack.setText(rack);

        if (coverImage.compareTo("") == 0) {
            imageView.setImage(new Image("dummy-book-cover.png"));
        } else {
            imageView.setImage(new Image(coverImage));
        }
    }

    void setParentController(HomeController controller) {
        parentController = controller;
    }

    Object getBook() {
        return root;
    }

    @FXML
    void onRemovePress() throws SQLException {
        if (parentController != null) {
            parentController.removeBook((Node) root, Integer.parseInt(bookID.getText()));
        }
    }
}
