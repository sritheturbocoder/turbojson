import exception.ArgumentNullException;
import exception.JsonException;

import java.util.Locale;
import java.util.Stack;

class JsonWriter {

    enum Condition
    {
        InArray,
        InObject,
        NotAProperty,
        Property,
        Value
    }

    static class WriterContext
    {
        public int  Count;
        public boolean InArray;
        public boolean InObject;
        public boolean ExpectingValue;
        public int  Padding;
    }

    private WriterContext        context;
    private Stack<WriterContext> ctx_stack;
    private boolean                 has_reached_end;
    private char[]               hex_seq;
    private int                  indentation;
    private int                  indent_value;
    private boolean                 pretty_print;
    private boolean                 validate;
    private boolean                 lower_case_properties;
    private final StringBuilder writer;

    public JsonWriter () throws ArgumentNullException {
        this(new StringBuilder ());
    }

    public JsonWriter (StringBuilder writer) throws ArgumentNullException {
        if (writer == null)
            throw new ArgumentNullException("writer");
        this.writer = writer;
        initialise();
    }

    private void initialise()
    {
        has_reached_end = false;
        hex_seq = new char[4];
        indentation = 0;
        indent_value = 4;
        pretty_print = false;
        validate = true;
        lower_case_properties = false;

        ctx_stack = new Stack<>();
        context = new WriterContext();
        ctx_stack.push (context);
    }

    private void intToHex(int n, char[] hex)
    {
        int num;

        for (int i = 0; i < 4; i++) {
            num = n % 16;

            if (num < 10)
                hex[3 - i] = (char) ('0' + num);
            else
                hex[3 - i] = (char) ('A' + (num - 10));

            n >>= 4;
        }
    }

    private void Indent ()
    {
        if (pretty_print)
            indentation += indent_value;
    }


    private void Put (String str)
    {
        if (pretty_print && ! context.ExpectingValue)
            writer.append (" ".repeat(Math.max(0, indentation)));

        writer.append (str);
    }

    private void PutNewline ()
    {
        PutNewline (true);
    }

    private void PutNewline (boolean add_comma)
    {
        if (add_comma && ! context.ExpectingValue &&
                context.Count > 1)
            writer.append (',');

        if (pretty_print && ! context.ExpectingValue)
            writer.append ("\n");
    }

    private void PutString (String str)
    {
        Put ("");

        writer.append ('"');

        int n = str.length();
        for (int i = 0; i < n; i++) {
            //noinspection EnhancedSwitchMigration
            switch (str.charAt(i)) {
                case '\n':
                    writer.append ("\\n");
                    continue;

                case '\r':
                    writer.append ("\\r");
                    continue;

                case '\t':
                    writer.append ("\\t");
                    continue;

                case '"':
                case '\\':
                    writer.append ('\\');
                    writer.append (str.charAt(i));
                    continue;

                case '\f':
                    writer.append ("\\f");
                    continue;

                case '\b':
                    writer.append ("\\b");
                    continue;
            }

            if ((int) str.charAt(i) >= 32 && (int) str.charAt(i) <= 126) {
                writer.append (str.charAt(i));
                continue;
            }

            intToHex (str.charAt(i), hex_seq);
            writer.append ("\\u");
            writer.append (hex_seq);
        }

        writer.append ('"');
    }

    private void Unindent ()
    {
        if (pretty_print)
            indentation -= indent_value;
    }

    public void Reset ()
    {
        has_reached_end = false;

        ctx_stack.clear ();
        context = new WriterContext();
        ctx_stack.push (context);

        if (writer != null)
            writer.delete (0, writer.length());
    }

    public void Write (boolean writeBool) throws JsonException {
        DoValidation (Condition.Value);
        PutNewline ();

        Put (writeBool ? "true" : "false");

        context.ExpectingValue = false;
    }

    public void Write (double number) throws JsonException {
        DoValidation (Condition.Value);
        PutNewline ();

        String str = Double.toString (number);
        Put (str);

        if (str.indexOf('.') == -1 &&
                str.indexOf ('E') == -1)
            writer.append (".0");

        context.ExpectingValue = false;
    }

    public void Write (int number) throws JsonException {
        DoValidation (Condition.Value);
        PutNewline ();

        Put (Integer.toString (number));

        context.ExpectingValue = false;
    }

    public void Write (long number) throws JsonException {
        DoValidation (Condition.Value);
        PutNewline ();

        Put (Long.toString (number));

        context.ExpectingValue = false;
    }

    public void Write (String str) throws JsonException {
        DoValidation (Condition.Value);
        PutNewline ();

        if (str == null)
            Put ("null");
        else
            PutString (str);

        context.ExpectingValue = false;
    }

    public void WriteArrayEnd () throws JsonException {
        DoValidation (Condition.InArray);
        PutNewline (false);

        ctx_stack.pop ();
        if (ctx_stack.size() == 1)
            has_reached_end = true;
        else {
            context = ctx_stack.peek ();
            context.ExpectingValue = false;
        }

        Unindent ();
        Put ("]");
    }

    public void WriteArrayStart () throws JsonException {
        DoValidation (Condition.NotAProperty);
        PutNewline ();

        Put ("[");

        context = new WriterContext();
        context.InArray = true;
        ctx_stack.push (context);

        Indent ();
    }

    public void WriteObjectEnd () throws JsonException {
        DoValidation (Condition.InObject);
        PutNewline (false);

        ctx_stack.pop ();
        if (ctx_stack.size() == 1)
            has_reached_end = true;
        else {
            context = ctx_stack.peek ();
            context.ExpectingValue = false;
        }

        Unindent ();
        Put ("}");
    }

    public void WriteObjectStart () throws JsonException {
        DoValidation (Condition.NotAProperty);
        PutNewline ();

        Put ("{");

        context = new WriterContext();
        context.InObject = true;
        ctx_stack.push (context);

        Indent ();
    }

    public void WritePropertyName (String property_name) throws JsonException {
        DoValidation (Condition.Property);
        PutNewline ();
        String propertyName = (property_name == null || !lower_case_properties)
                ? property_name
                : property_name.toLowerCase(Locale.ROOT);

        PutString (propertyName);

        if (pretty_print) {
            if (propertyName.length() > context.Padding)
                context.Padding = propertyName.length();

            writer.append (" ".repeat(Math.max(0, context.Padding - propertyName.length() + 1)));

            writer.append (": ");
        } else
            writer.append (':');

        context.ExpectingValue = true;
    }

    private void DoValidation (Condition cond) throws JsonException {
        if (! context.ExpectingValue)
            context.Count++;

        if (! validate)
            return;

        if (has_reached_end)
            throw new JsonException(
                    "A complete JSON symbol has already been written");

        switch (cond) {
            case InArray:
                if (! context.InArray)
                    throw new JsonException (
                            "Can't close an array here");
                break;

            case InObject:
                if (! context.InObject || context.ExpectingValue)
                    throw new JsonException (
                            "Can't close an object here");
                break;

            case NotAProperty:
                if (context.InObject && ! context.ExpectingValue)
                    throw new JsonException (
                            "Expected a property");
                break;

            case Property:
                if (! context.InObject || context.ExpectingValue)
                    throw new JsonException (
                            "Can't add a property here");
                break;

            case Value:
                if (! context.InArray &&
                        (! context.InObject || ! context.ExpectingValue))
                    throw new JsonException (
                            "Can't add a value here");

                break;
            default:
                throw new IllegalStateException("Unexpected value: " + cond);
        }
    }
}
