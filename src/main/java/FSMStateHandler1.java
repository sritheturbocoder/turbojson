import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class FSMStateHandler1 implements Callable<Boolean>, IFSMStateHandler {

    private final FSMContext ctx;
    private final ExecutorService executor;

    public FSMStateHandler1(FSMContext ctx, ExecutorService executor) {
        this.ctx = ctx;
        this.executor = executor;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Boolean call() throws Exception {
        return this.state1();
    }

    public boolean state1() throws IOException {
        while (ctx.lexer.getChar()) {
            if (ctx.lexer.getInputChar() == ' ' ||
                    ctx.lexer.getInputChar() >= '\t' && ctx.lexer.getInputChar() <= '\r')
                continue;

            if (ctx.lexer.getInputChar() >= '1' && ctx.lexer.getInputChar() <= '9') {
                ctx.lexer.getStringBuilder().append((char) ctx.lexer.getInputChar());
                ctx.nextState = 3;
                return true;
            }

            switch (ctx.lexer.getInputChar()) {
                case '"':
                    ctx.nextState = 19;
                    ctx.fsmReturn = true;
                    return true;

                case ',':
                case ':':
                case '[':
                case ']':
                case '{':
                case '}':
                    ctx.nextState = 1;
                    ctx.fsmReturn = true;
                    return true;

                case '-':
                    ctx.lexer.getStringBuilder().append((char) ctx.lexer.getInputChar());
                    ctx.nextState = 2;
                    return true;

                case '0':
                    ctx.lexer.getStringBuilder().append((char) ctx.lexer.getInputChar());
                    ctx.nextState = 4;
                    return true;

                case 'f':
                    ctx.nextState = 12;
                    return true;

                case 'n':
                    ctx.nextState = 16;
                    return true;

                case 't':
                    ctx.nextState = 9;
                    return true;

                case '\'':
                    if (!ctx.lexer.getAllowSingleQuotedStrings())
                        return false;

                    ctx.lexer.setInputChar('"');
                    ctx.nextState = 23;
                    ctx.fsmReturn = true;
                    return true;

                case '/':
                    if (!ctx.lexer.getAllowComments())
                        return false;

                    ctx.nextState = 25;
                    return true;

                default:
                    return false;
            }
        }

        return true;
    }

    @Override
    public Future<Boolean> submitTask() {
        return executor.submit(this);
    }
}
