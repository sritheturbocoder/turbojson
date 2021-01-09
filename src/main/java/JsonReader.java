import exception.JsonException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ExecutionException;

/**
 * Stream like access to JSON text
*/
class JsonReader {

    public boolean isSkip_non_members() {
        return skip_non_members;
    }

    public void setSkip_non_members(boolean skip_non_members) {
        this.skip_non_members = skip_non_members;
    }

    public boolean getAllowComments() {
        return this.lexer.getAllowComments();
    }

    public void setAllowComments(boolean allowcomments) {
        this.lexer.setAllowComments(allowcomments);
    }

    public boolean getSingleQuotedString() {
        return this.lexer.getAllowSingleQuotedStrings();
    }

    public void setSingleQuotedString(boolean allowSingleQuote) {
        this.lexer.setAllowSingleQuotedStrings(allowSingleQuote);
    }

    public boolean isEnd_of_input() {
        return end_of_input;
    }

    public boolean isEnd_of_json() {
        return end_of_json;
    }

    public JsonToken getToken() {
        return token;
    }

    public Object getToken_value() {
        return token_value;
    }

    private Map<Integer, HashMap<Integer, int[]>> parse_table;

    private Stack<Integer> automaton_stack;
    private int           current_input;
    private int           current_symbol;
    private boolean          end_of_json;
    private boolean          end_of_input;
    private Lexer         lexer;
    private boolean          parser_in_string;
    private boolean          parser_return;
    private boolean          read_started;
    private Reader reader;
    private boolean          reader_is_owned;
    private boolean          skip_non_members;
    private Object        token_value;
    private JsonToken     token;

    JsonReader ()
    {

    }

    public JsonReader (String json_text)

    {
        this (new StringReader(json_text), true);
    }

    public JsonReader (Reader reader)

    {
        this (reader, false);
    }

    private JsonReader (Reader reader, boolean owned)
    {
        parse_table = initialiseParseTable();
        if (reader == null)
            throw new IllegalArgumentException ("reader");

        parser_in_string = false;
        parser_return    = false;

        read_started = false;
        automaton_stack = new Stack<>();
        automaton_stack.push (ParserToken.End);
        automaton_stack.push (ParserToken.Text);

        lexer = new Lexer (reader);

        end_of_input = false;
        end_of_json  = false;

        skip_non_members = true;

        this.reader = reader;
        reader_is_owned = owned;
    }

    private Map<Integer, HashMap<Integer, int[]>> initialiseParseTable()
    {
        Map<Integer, HashMap<Integer, int[]>> parse_table = new HashMap<>();

        tableAddRow (parse_table, ParserToken.Array);
        tableAddCol (parse_table, ParserToken.Array, '[',
                '[',
                ParserToken.ArrayPrime);

        tableAddRow (parse_table, ParserToken.ArrayPrime);
        tableAddCol (parse_table, ParserToken.ArrayPrime, '"',
                ParserToken.Value,

                ParserToken.ValueRest,
                ']');
        tableAddCol (parse_table, ParserToken.ArrayPrime, '[',
                ParserToken.Value,
                ParserToken.ValueRest,
                ']');
        tableAddCol (parse_table, ParserToken.ArrayPrime, ']',
                ']');
        tableAddCol (parse_table, ParserToken.ArrayPrime, '{',
                ParserToken.Value,
                ParserToken.ValueRest,
                ']');
        tableAddCol (parse_table, ParserToken.ArrayPrime, ParserToken.Number,
                ParserToken.Value,
                ParserToken.ValueRest,
                ']');
        tableAddCol (parse_table, ParserToken.ArrayPrime, ParserToken.True,
                ParserToken.Value,
                ParserToken.ValueRest,
                ']');
        tableAddCol (parse_table, ParserToken.ArrayPrime, ParserToken.False,
                ParserToken.Value,
                ParserToken.ValueRest,
                ']');
        tableAddCol (parse_table, ParserToken.ArrayPrime, ParserToken.Null,
                ParserToken.Value,
                ParserToken.ValueRest,
                ']');

        tableAddRow (parse_table, ParserToken.Object);
        tableAddCol (parse_table, ParserToken.Object, '{',
                '{',
                ParserToken.ObjectPrime);

        tableAddRow (parse_table, ParserToken.ObjectPrime);
        tableAddCol (parse_table, ParserToken.ObjectPrime, '"',
                ParserToken.Pair,
                ParserToken.PairRest,
                '}');
        tableAddCol (parse_table, ParserToken.ObjectPrime, '}',
                '}');

        tableAddRow (parse_table, ParserToken.Pair);
        tableAddCol (parse_table, ParserToken.Pair, '"',
                ParserToken.String,
                ':',
                ParserToken.Value);

        tableAddRow (parse_table, ParserToken.PairRest);
        tableAddCol (parse_table, ParserToken.PairRest, ',',
                ',',
                ParserToken.Pair,
                ParserToken.PairRest);
        tableAddCol (parse_table, ParserToken.PairRest, '}',
                ParserToken.Epsilon);

        tableAddRow (parse_table, ParserToken.String);
        tableAddCol (parse_table, ParserToken.String, '"',
                '"',
                ParserToken.CharSeq,
                '"');

        tableAddRow (parse_table, ParserToken.Text);
        tableAddCol (parse_table, ParserToken.Text, '[',
                ParserToken.Array);
        tableAddCol (parse_table, ParserToken.Text, '{',
                ParserToken.Object);

        tableAddRow (parse_table, ParserToken.Value);
        tableAddCol (parse_table, ParserToken.Value, '"',
                ParserToken.String);
        tableAddCol (parse_table, ParserToken.Value, '[',
                ParserToken.Array);
        tableAddCol (parse_table, ParserToken.Value, '{',
                ParserToken.Object);
        tableAddCol (parse_table, ParserToken.Value, ParserToken.Number,
                ParserToken.Number);
        tableAddCol (parse_table, ParserToken.Value, ParserToken.True,
                ParserToken.True);
        tableAddCol (parse_table, ParserToken.Value, ParserToken.False,
                ParserToken.False);
        tableAddCol (parse_table, ParserToken.Value, ParserToken.Null,
                ParserToken.Null);

        tableAddRow (parse_table, ParserToken.ValueRest);
        tableAddCol (parse_table, ParserToken.ValueRest, ',',
                ',',
                ParserToken.Value,
                ParserToken.ValueRest);
        tableAddCol (parse_table, ParserToken.ValueRest, ']',
                ParserToken.Epsilon);

        return parse_table;
    }

    private static void tableAddCol (Map<Integer, HashMap<Integer, int[]>> parse_table, int row, int col, int... symbols)
    {
        parse_table.get(row).put(col, symbols);
    }

    private static void tableAddRow (Map<Integer, HashMap<Integer, int[]>> parse_table, int rule)
    {
        parse_table.put (rule, new HashMap<> ());
    }

    private void ProcessNumber (String number)
    {
        try {
            if (processDouble(number)) return;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        try {
            if (processInteger(number)) return;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        try {
            if (processLong(Long.parseLong(number))) return;
        }catch (NumberFormatException e) {
            e.printStackTrace();
        }

        try {
            if (processLong(Long.parseUnsignedLong(number))) return;
        }catch (NumberFormatException e) {
            e.printStackTrace();
        }

        // Shouldn't happen, but just in case, return something
        token = JsonToken.Int;
        token_value = 0;
    }

    private boolean processDouble(String number) {
        if (number.indexOf ('.') != -1 ||
                number.indexOf ('e') != -1 ||
                number.indexOf ('E') != -1) {

            double n_double = Double.parseDouble(number);
            token = JsonToken.Double;
            token_value = n_double;
            return true;
        }
        return false;
    }

    private boolean processInteger(String number) {
        int n_int32 = Integer.parseInt(number);
        token = JsonToken.Int;
        token_value = n_int32;
        return true;
    }

    private boolean processLong(long l) {
        token = JsonToken.Long;
        token_value = l;
        return true;
    }

    private void ProcessSymbol ()
    {
        if (current_symbol == '[')  {
            token = JsonToken.ArrayStart;
            parser_return = true;

        } else if (current_symbol == ']')  {
            token = JsonToken.ArrayEnd;
            parser_return = true;

        } else if (current_symbol == '{')  {
            token = JsonToken.ObjectStart;
            parser_return = true;

        } else if (current_symbol == '}')  {
            token = JsonToken.ObjectEnd;
            parser_return = true;

        } else if (current_symbol == '"')  {
            if (parser_in_string) {
                parser_in_string = false;

                parser_return = true;

            } else {
                if (token == JsonToken.None)
                    token = JsonToken.String;

                parser_in_string = true;
            }

        } else if (current_symbol == ParserToken.CharSeq) {
            token_value = lexer.getStringValue();

        } else if (current_symbol == ParserToken.False)  {
            token = JsonToken.Boolean;
            token_value = false;
            parser_return = true;

        } else if (current_symbol == ParserToken.Null)  {
            token = JsonToken.Null;
            parser_return = true;

        } else if (current_symbol == ParserToken.Number)  {
            ProcessNumber (lexer.getStringValue());

            parser_return = true;

        } else if (current_symbol == ParserToken.Pair)  {
            token = JsonToken.PropertyName;

        } else if (current_symbol == ParserToken.True)  {
            token = JsonToken.Boolean;
            token_value = true;
            parser_return = true;

        }
    }

    private boolean readToken() throws InterruptedException, ExecutionException, JsonException, IOException {
        if (end_of_input)
            return true;

        lexer.NextToken ();

        if (lexer.isEndOfInput()) {
            close();

            return true;
        }

        current_input = lexer.getToken();

        return false;
    }

    public void close() throws IOException {
        if (end_of_input)
            return;

        end_of_input = true;
        end_of_json  = true;

        reader.close();

        reader = null;
    }

    public boolean Read () throws InterruptedException, ExecutionException, JsonException, IOException {
        if (end_of_input)
            return false;

        if (end_of_json) {
            end_of_json = false;
            automaton_stack.clear ();
            automaton_stack.push (ParserToken.End);
            automaton_stack.push (ParserToken.Text);
        }

        parser_in_string = false;
        parser_return    = false;

        token       = JsonToken.None;
        token_value = null;

        if (! read_started) {
            read_started = true;

            if (readToken())
                return false;
        }


        int[] entry_symbols;

        while (true) {
            if (parser_return) {
                if (automaton_stack.peek() == ParserToken.End)
                    end_of_json = true;

                return true;
            }

            current_symbol = automaton_stack.pop ();

            ProcessSymbol ();

            if (current_symbol == current_input) {
                if (readToken()) {
                    if (automaton_stack.peek () != ParserToken.End)
                        throw new JsonException ("Input doesn't evaluate to proper JSON text");

                    return parser_return;
                }

                continue;
            }

            entry_symbols =
                    parse_table.get(current_symbol).get(current_input);

            if (entry_symbols[0] == ParserToken.Epsilon)
                continue;

            for (int i = entry_symbols.length - 1; i >= 0; i--)
                automaton_stack.push (entry_symbols[i]);
        }
    }
}