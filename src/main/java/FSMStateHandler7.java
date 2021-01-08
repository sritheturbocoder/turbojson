import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class FSMStateHandler7 implements Callable<Boolean>, IFSMStateHandler {

    private FSMContext ctx;
    private ExecutorService executor;

    public FSMStateHandler7(FSMContext ctx, ExecutorService executor) {
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
        return this.state7();
    }

    private boolean state7() throws IOException {
        ctx.lexer.getChar();

        if (ctx.lexer.getInputChar() >= '0' && ctx.lexer.getInputChar() <= '9') {
            ctx.lexer.getStringBuilder().append ((char) ctx.lexer.getInputChar());
            ctx.nextState = 8;
            return true;
        }

        switch (ctx.lexer.getInputChar()) {
            case '+':
            case '-':
                ctx.lexer.getStringBuilder().append ((char) ctx.lexer.getInputChar());
                ctx.nextState = 8;
                return true;

            default:
                return false;
        }
    }

    @Override
    public Future<Boolean> submitTask() {
        return executor.submit(this);
    }
}
