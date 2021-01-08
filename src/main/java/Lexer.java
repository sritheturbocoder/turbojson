import exception.JsonException;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class FSMContext
{
    public boolean fsmReturn;
    public int nextState;
    public Lexer lexer;
    public int stateStack;
}

class ParserToken
{
    // Lexer tokens (see section A.1.1. of the manual)
    public static final int None = Character.MAX_VALUE + 1;

    public static final int Number = 65537;

    public static final int True = 65538;

    public static final int False = 65539;

    public static final int Null = 65540;

    public static final int CharSeq = 65541;

    public static final int Char = 65542;

    // Parser Rules (see section A.2.1 of the manual)
    public static final int Text = 65543;

    public static final int Object = 65544;

    public static final int ObjectPrime = 65545;

    public static final int Pair = 65546;

    public static final int PairRest = 65547;

    public static final int Array = 65548;

    public static final int ArrayPrime = 65549;

    public static final int Value = 65550;

    public static final int ValueRest = 65551;

    public static final int String = 65552;

    public static final int End = 65553;

    public static final int Epsilon = 65554;
}

class Lexer
{
    private int[] fsmTable = new int[28];
    private List<IFSMStateHandler> fsmHandler;

    private boolean allowComments;
    private boolean allowSingleQuotedStrings;
    private boolean endOfInput;

    private FSMContext fsmContext;

    private int inputBuffer;
    private int inputChar;
    private int state;
    private int token;
    private int unichar;

    private Reader reader;

    private StringBuilder stringBuilder;
    private String stringValue;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    public Lexer (Reader reader)
    {
        setAllowComments(true);
        setAllowSingleQuotedStrings(true);

        inputBuffer = 0;
        stringBuilder = new StringBuilder (128);
        state = 1;
        setEndOfInput(false);
        this.reader = reader;

        fsmContext = new FSMContext();
        initializeFSMTable();
        initializeFSMHandler();
        fsmContext.lexer = this;
    }

    private void initializeFSMHandler() {
        fsmHandler = new ArrayList<>();
        fsmHandler.add(new FSMStateHandler1(fsmContext, executor));
        fsmHandler.add(new FSMStateHandler2(fsmContext, executor));
        fsmHandler.add(new FSMStateHandler3(fsmContext, executor));
        fsmHandler.add(new FSMStateHandler4(fsmContext, executor));
        fsmHandler.add(new FSMStateHandler5(fsmContext, executor));
        fsmHandler.add(new FSMStateHandler6(fsmContext, executor));
        fsmHandler.add(new FSMStateHandler7(fsmContext, executor));
        fsmHandler.add(new FSMStateHandler8(fsmContext, executor));
        fsmHandler.add(new FSMStateHandler9(fsmContext, executor));
        fsmHandler.add(new FSMStateHandler10(fsmContext, executor));
        fsmHandler.add(new FSMStateHandler11(fsmContext, executor));
        fsmHandler.add(new FSMStateHandler12(fsmContext, executor));
        fsmHandler.add(new FSMStateHandler13(fsmContext, executor));
        fsmHandler.add(new FSMStateHandler14(fsmContext, executor));
        fsmHandler.add(new FSMStateHandler15(fsmContext, executor));
        fsmHandler.add(new FSMStateHandler16(fsmContext, executor));
        fsmHandler.add(new FSMStateHandler17(fsmContext, executor));
        fsmHandler.add(new FSMStateHandler18(fsmContext, executor));
        fsmHandler.add(new FSMStateHandler19(fsmContext, executor));
        fsmHandler.add(new FSMStateHandler20(fsmContext, executor));
        fsmHandler.add(new FSMStateHandler21(fsmContext, executor));
        fsmHandler.add(new FSMStateHandler22(fsmContext, executor));
        fsmHandler.add(new FSMStateHandler23(fsmContext, executor));
        fsmHandler.add(new FSMStateHandler24(fsmContext, executor));
        fsmHandler.add(new FSMStateHandler25(fsmContext, executor));
        fsmHandler.add(new FSMStateHandler26(fsmContext, executor));
        fsmHandler.add(new FSMStateHandler27(fsmContext, executor));
        fsmHandler.add(new FSMStateHandler28(fsmContext, executor));
    }

    private void initializeFSMTable() {
        fsmTable[0] = ParserToken.Char;
        fsmTable[1] = 0;
        fsmTable[2] = ParserToken.Number;
        fsmTable[3] = ParserToken.Number;
        fsmTable[4] = 0;
        fsmTable[5] = ParserToken.Number;
        fsmTable[6] = 0;
        fsmTable[7] = ParserToken.Number;
        fsmTable[8] =0;
        fsmTable[9] =0;
        fsmTable[10] = ParserToken.True;
        fsmTable[11] =0;
        fsmTable[12] =0;
        fsmTable[13] =0;
        fsmTable[14] = ParserToken.False;
        fsmTable[15] =0;
        fsmTable[16] =0;
        fsmTable[17] = ParserToken.Null;
        fsmTable[18] = ParserToken.CharSeq;
        fsmTable[19] = ParserToken.Char;
        fsmTable[20] =0;
        fsmTable[21] =0;
        fsmTable[22] = ParserToken.CharSeq;
        fsmTable[23] = ParserToken.Char;
        fsmTable[24] =0;
        fsmTable[25] =0;
        fsmTable[26] =0;
        fsmTable[27] =0;
    }

    public void ungetChar() {
        inputBuffer = inputChar;
    }

    public boolean getChar() throws IOException {
        if ((inputChar = NextChar ()) != -1)
            return true;
        setEndOfInput(true);
        return false;
    }

    private int NextChar () throws IOException {
        if (inputBuffer != 0) {
            int tmp = inputBuffer;
            inputBuffer = 0;

            return tmp;
        }

        return reader.read ();
    }

    public boolean NextToken () throws JsonException, ExecutionException, InterruptedException {

        fsmContext.fsmReturn = false;

        while (true) {
            IFSMStateHandler handler = fsmHandler.get(state - 1);
            Future<Boolean> booleanFuture = handler.submitTask();
            if (! booleanFuture.get())
                throw new JsonException(getInputChar());
            if (isEndOfInput())
                return false;
            if (fsmContext.fsmReturn) {
                setStringValue(getStringBuilder().toString ());
                getStringBuilder().delete (0, getStringBuilder().length());
                setToken(fsmTable[state - 1]);

                if (getToken() == ParserToken.Char)
                    setToken(getInputChar());

                state = fsmContext.nextState;

                return true;
            }

            state = fsmContext.nextState;
        }
    }

    public boolean isAllowComments() {
        return allowComments;
    }

    public void setAllowComments(boolean allowComments) {
        this.allowComments = allowComments;
    }

    public boolean isAllowSingleQuotedStrings() {
        return allowSingleQuotedStrings;
    }

    public void setAllowSingleQuotedStrings(boolean allowSingleQuotedStrings) {
        this.allowSingleQuotedStrings = allowSingleQuotedStrings;
    }

    public boolean isEndOfInput() {
        return endOfInput;
    }

    public void setEndOfInput(boolean endOfInput) {
        this.endOfInput = endOfInput;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public int getToken() {
        return token;
    }

    public void setToken(int token) {
        this.token = token;
    }

    public int getInputChar() {
        return inputChar;
    }

    public void setInputChar(int c) {
        inputChar = c;
    }

    public StringBuilder getStringBuilder() {
        return stringBuilder;
    }

    public void setUnichar(int unichar) {
        this.unichar = unichar;
    }

    public int getUnichar() {
        return unichar;
    }
}