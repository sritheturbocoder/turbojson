import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class FSMStateHandler23 implements Callable<Boolean>, IFSMStateHandler {

    private final FSMContext ctx;
    private final ExecutorService executor;

    public FSMStateHandler23(FSMContext ctx, ExecutorService executor) {
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
        return this.state23();
    }

    private boolean state23() throws IOException {
        while (ctx.lexer.getChar()) {
            switch (ctx.lexer.getInputChar()) {
                case '\'' -> {
                    ctx.lexer.ungetChar();
                    ctx.fsmReturn = true;
                    ctx.nextState = 24;
                    return true;
                }
                case '\\' -> {
                    ctx.stateStack = 23;
                    ctx.nextState = 21;
                    return true;
                }
                default -> ctx.lexer.getStringBuilder().append((char) ctx.lexer.getInputChar());
            }
        }

        return true;
    }

    @Override
    public Future<Boolean> submitTask() {
        return executor.submit(this);
    }
}
