package athensclub.calculator.controllers;

import ch.obermuhlner.math.big.DefaultBigDecimalMath;
import com.jfoenix.controls.JFXButton;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;

import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;

public class MainViewController implements Initializable {

    private static final Logger LOG = Logger.getLogger(MainViewController.class.getName());

    @FXML
    private BorderPane root;

    @FXML
    private GridPane grid;

    @FXML
    private TextField outputText;

    @FXML
    private Label hintText;

    @FXML
    private Text infoText;

    private JFXButton delete;

    private JFXButton dot;

    private JFXButton equal;

    private JFXButton power;

    private JFXButton reciprocal;

    private JFXButton clear;

    private boolean expectSecondNumber;

    private SimpleObjectProperty<BigDecimal> secondValue;

    private SimpleObjectProperty<BigDecimal> currentValue;

    private SimpleStringProperty currentOperatorString;

    private BiFunction<BigDecimal, BigDecimal, BigDecimal> currentOperator;

    private HashMap<Integer, JFXButton> numberToButton;

    private HashMap<String, JFXButton> operatorToButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentValue = new SimpleObjectProperty<>();
        secondValue = new SimpleObjectProperty<>();
        currentOperatorString = new SimpleStringProperty("");
        numberToButton = new HashMap<>();
        operatorToButton = new HashMap<>();

        SimpleDoubleProperty rootLeftPlusRightPadding = new SimpleDoubleProperty();
        root.paddingProperty().addListener((prop, old, val) ->
                rootLeftPlusRightPadding.set(val.getLeft() + val.getRight()));
        infoText.wrappingWidthProperty().bind(root.widthProperty().subtract(rootLeftPlusRightPadding));

        StringExpression blankString = new SimpleStringProperty(""); // always blank
        StringExpression afterOperator = Bindings
                .when(secondValue.isNull())
                .then(blankString)
                .otherwise(secondValue.asString());
        StringExpression afterFirstValue = Bindings
                .when(currentOperatorString.isEmpty())
                .then(blankString)
                .otherwise(Bindings.concat(" ", currentOperatorString, " ", afterOperator));
        hintText.textProperty().bind(Bindings
                .when(currentValue.isNull())
                .then(blankString)
                .otherwise(currentValue.asString().concat(afterFirstValue)));

        addButtons();
    }

    /**
     * Create an {@link EventHandler<KeyEvent>} instance that will bind key input to
     * the button input.
     *
     * @return an {@link EventHandler<KeyEvent>} binding key input to the button input.
     */
    public EventHandler<KeyEvent> createKeyBindHandler() {
        return e -> {
            if (e.getEventType() == KeyEvent.KEY_PRESSED) {
                if (e.getCode() == KeyCode.BACK_SPACE || e.getCode() == KeyCode.DELETE)
                    delete.fire();
            } else if (e.getEventType() == KeyEvent.KEY_TYPED) {
                String s = e.getCharacter();
                if (s.length() == 0) {
                    LOG.warning("key character length = 0 ");
                    return;
                }
                if (s.length() > 1) {
                    LOG.warning("key character length > 1: " + s);
                    return;
                }

                if (Character.isDigit(s.codePointAt(0))) {
                    numberToButton.get(Integer.parseInt(s)).fire();
                } else if (operatorToButton.containsKey(s)) {
                    operatorToButton.get(s).fire();
                } else {
                    switch (s) {
                        case "." -> dot.fire();
                        case "=" -> equal.fire();
                    }
                }
            }
        };
    }

    private EventHandler<ActionEvent> createNumberEventHandler(int number) {
        String num = Integer.toString(number);
        return e -> {
            if (!expectSecondNumber && currentOperator != null) {
                // finished with current calculation, user pressed the number to perform new calculation
                reset();
            }
            String text = outputText.getText();
            outputText.setText(text.equals("0") ? num : text + num);
        };
    }


    private BigDecimal outputTextToBigDecimal() {
        String text = outputText.getText();
        return new BigDecimal(text + (text.endsWith(".") ? "0" : ""));
    }

    private void reset() {
        currentValue.set(null);
        secondValue.set(null);
        currentOperatorString.set("");
        currentOperator = null;
        outputText.setText("0");
    }

    private void clearClicked(ActionEvent e) {
        reset();
    }

    private void equalClicked(ActionEvent e) {
        try {
            if (!expectSecondNumber) {
                if (currentOperatorString.get().isBlank()) {
                    currentValue.set(outputTextToBigDecimal());
                    outputText.setText("0");
                } else {
                    currentValue.set(currentOperator.apply(currentValue.get(), secondValue.get()));
                    outputText.setText(currentValue.get().toEngineeringString());
                }
            } else {
                secondValue.set(outputTextToBigDecimal());
                currentValue.set(currentOperator.apply(currentValue.get(), secondValue.get()));
                outputText.setText(currentValue.get().toString());
                expectSecondNumber = false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            outputText.setText("ERROR");
        }
    }

    private void operatorClicked(String text, BiFunction<BigDecimal, BigDecimal, BigDecimal> operation) {
        try {
            if (currentValue.isNull().get())
                currentValue.set(outputTextToBigDecimal());
            outputText.setText("0");
            currentOperatorString.set(text);
            currentOperator = operation;
            expectSecondNumber = true;
        } catch (Exception e) {
            e.printStackTrace();
            outputText.setText("ERROR");
        }
    }

    private void functionClicked(Function<BigDecimal, BigDecimal> function) {
        try {
            outputText.setText(function.apply(outputTextToBigDecimal()).toString());
            if (!expectSecondNumber) {
                currentOperatorString.set("");
                currentOperator = null;
                currentValue.set(outputTextToBigDecimal());
            }
        } catch (Exception e) {
            e.printStackTrace();
            outputText.setText("ERROR");
        }
    }

    private void addButtons() {
        String mathX = "" + Character.toString(Integer.parseInt("1D465", 16));
        int numOffX = 0, numOffY = 1, operatorOffX = numOffX + 3, operatorOffY = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int number = 9 - (3 * j + (2 - i));
                JFXButton button = new JFXButton(Integer.toString(number));
                button.addEventHandler(ActionEvent.ACTION, createNumberEventHandler(number));
                button.getStyleClass().addAll("cell", "number-button");
                grid.add(button, i + numOffX, j + numOffY, 1, 1);
                numberToButton.put(number, button);
            }
        }
        JFXButton zero = new JFXButton("0");
        zero.addEventHandler(ActionEvent.ACTION, createNumberEventHandler(0));
        zero.getStyleClass().addAll("cell", "number-button");
        grid.add(zero, 1 + numOffX, 3 + numOffY);
        numberToButton.put(0, zero);

        dot = new JFXButton(".");
        dot.addEventHandler(ActionEvent.ACTION, e -> {
            String text = outputText.getText();
            outputText.setText(text.contains(".") ? text : text + ".");
        });
        dot.getStyleClass().addAll("cell", "operator-button");
        grid.add(dot, 2 + numOffX, 3 + numOffY);

        equal = new JFXButton("=");
        equal.getStyleClass().addAll("cell", "equal-button");
        equal.addEventHandler(ActionEvent.ACTION, this::equalClicked);
        grid.add(equal, numOffX, 3 + numOffY);

        power = new JFXButton(mathX + '\u02B8');
        power.getStyleClass().addAll("cell", "operator-button");
        power.addEventHandler(ActionEvent.ACTION, e ->
                operatorClicked("^", (a, b) -> DefaultBigDecimalMath.pow(a, b)));
        grid.add(power, numOffX, operatorOffY);
        operatorToButton.put("^", power);

        reciprocal = new JFXButton("" + '\u215F' + '\u2093');
        reciprocal.getStyleClass().addAll("cell", "operator-button");
        reciprocal.addEventHandler(ActionEvent.ACTION, e ->
                functionClicked(a -> BigDecimal.ONE.divide(a, MathContext.DECIMAL128)));
        grid.add(reciprocal, numOffX + 1, operatorOffY);

        clear = new JFXButton("C");
        clear.getStyleClass().addAll("cell", "operator-button");
        clear.addEventHandler(ActionEvent.ACTION, this::clearClicked);
        grid.add(clear, numOffX + 2, operatorOffY);

        delete = new JFXButton("" + '\u232B');
        delete.addEventHandler(ActionEvent.ACTION, e -> {
            String text = outputText.getText();
            outputText.setText(text.length() == 1 ? "0" : text.substring(0, text.length() - 1));
        });
        delete.getStyleClass().addAll("cell", "operator-button");
        grid.add(delete, operatorOffX, operatorOffY);

        // following array and list will be in the same order (+ - * /)
        String[] operators = {"+", "-", "" + '\u00D7', "" + '\u00F7'}; // + - * /
        String[] operatorKeys = {"" + '\uFF0B', "-", "*", "/"};
        List<BiFunction<BigDecimal, BigDecimal, BigDecimal>> operatorFunction =
                List.of(BigDecimal::add, BigDecimal::subtract, BigDecimal::multiply, (a, b) -> a.divide(b, MathContext.DECIMAL128));
        for (int i = 0; i < operators.length; i++) {
            JFXButton button = new JFXButton(operators[i]);
            operatorToButton.put(operators[i], button);
            operatorToButton.put(operatorKeys[i], button);
            button.getStyleClass().addAll("cell", "operator-button");
            final int currentIndex = i;
            button.addEventHandler(ActionEvent.ACTION, e ->
                    operatorClicked(operators[currentIndex], operatorFunction.get(currentIndex)));
            grid.add(button, operatorOffX, operatorOffY + i + 1);
        }

        //fill the constraints list so that we can use list.set method.
        while (grid.getColumnConstraints().size() < grid.getColumnCount())
            grid.getColumnConstraints().add(new ColumnConstraints());
        while (grid.getRowConstraints().size() < grid.getRowCount())
            grid.getRowConstraints().add(new RowConstraints());


        for (int i = 0; i < grid.getRowCount(); i++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(100.0 / grid.getRowCount());
            grid.getRowConstraints().set(i, rc); // set because adding will cause infinite loop.
        }

        for (int i = 0; i < grid.getColumnCount(); i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / grid.getColumnCount());
            grid.getColumnConstraints().set(i, cc); // set because adding will cause infinite loop.
        }
    }

}
