module tg.univlome.epl.bypedu {
    requires javafx.controls;
    requires javafx.fxml;

    opens tg.univlome.epl.bypedu to javafx.fxml;
    exports tg.univlome.epl.bypedu;
}
