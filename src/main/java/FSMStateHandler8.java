import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class FSMStateHandler8 implements Callable<Boolean>, IFSMStateHandler {

    private final FSMContext ctx;
    private final ExecutorService executor;

    public FSMStateHandler8(FSMContext ctx, ExecutorService executor) {
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
        return this.state8();
    }

    private boolean state8 () throws IOException {
        while (ctx.lexer.getChar()) {
            if (ctx.lexer.getInputChar() >= '0' && ctx.lexer.getInputChar() <= '9') {
                ctx.lexer.getStringBuilder().append ((char) ctx.lexer.getInputChar());
                continue;
            }

            if (ctx.lexer.getInputChar() == ' ' ||
                    ctx.lexer.getInputChar() >= '\t' && ctx.lexer.getInputChar() <= '\r') {
                ctx.fsmReturn = true;
                ctx.nextState = 1;
                return true;
            }

            switch (ctx.lexer.getInputChar()) {
                case ',':
                case ']':
                case '}':
                    ctx.lexer.ungetChar();
                    ctx.fsmReturn = true;
                    ctx.nextState = 1;
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
