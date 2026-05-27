module tg.univlome.epl.bypedu {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens tg.univlome.epl.bypedu to javafx.fxml;
    exports tg.univlome.epl.bypedu;
}
