import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class FSMStateHandler19 implements Callable<Boolean>, IFSMStateHandler {

    private final FSMContext ctx;
    private final ExecutorService executor;

    public FSMStateHandler19(FSMContext ctx, ExecutorService executor) {
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
        return this.state19();
    }

    private boolean state19() throws IOException {
        while (ctx.lexer.getChar()) {
            if (ctx.lexer.getInputChar() == '"') {
                ctx.lexer.ungetChar();
                ctx.fsmReturn = true;
                ctx.nextState = 20;
                return true;
            } else if (ctx.lexer.getInputChar() == '\\') {
                ctx.stateStack = 19;
                ctx.nextState = 21;
                return true;
            }
            ctx.lexer.getStringBuilder().append((char) ctx.lexer.getInputChar());
        }

        return true;
    }

    @Override
    public Future<Boolean> submitTask() {
        return executor.submit(this);
    }
}
