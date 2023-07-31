import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.junit.jupiter.api.Test;

public class SerializeTest {
    @Test
    public void test() {


    }

}

class Car {

    private String color;
    private String type;

    public Car(String color, String type) {
        this.color = color;
        this.type = type;
    }

    public Car() {

    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Car{" +
                "color='" + color + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
    // standard getters setters
}

@Data
@AllArgsConstructor
@ToString
class UUID {
    private String uuid;
    private String data;
}
