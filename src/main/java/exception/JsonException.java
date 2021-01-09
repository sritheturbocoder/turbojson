package exception;

public class JsonException extends Throwable {
    public JsonException(int inputChar) {
        super(String.valueOf(inputChar));
    }

    public JsonException(int inputChar, KeyNotFoundException e) {
        super(String.valueOf(inputChar));
    }

    public JsonException(String s) {
        super(s);
    }
}