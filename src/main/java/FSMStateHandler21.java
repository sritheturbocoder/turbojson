import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class FSMStateHandler21 implements Callable<Boolean>, IFSMStateHandler {

    private final FSMContext ctx;
    private final ExecutorService executor;

    public FSMStateHandler21(FSMContext ctx, ExecutorService executor) {
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
        return this.state21();
    }

    private boolean state21()
    {
        ctx.L.getChar();

        switch (ctx.L.getInputChar()) {
            case 'u':
                ctx.nextState = 22;
                return true;

            case '"':
            case '\'':
            case '/':
            case '\\':
            case 'b':
            case 'f':
            case 'n':
            case 'r':
            case 't':
                ctx.L.getStringBuilder().append (
                        processEscChar(ctx.L.getInputChar()));
                ctx.nextState = ctx.StateStack;
                return true;

            default:
                return false;
        }
    }

    private static char processEscChar(int esc_char)
    {
        // Unreachable
        return switch (esc_char) {
            case '"', '\'', '\\', '/' -> (char) esc_char;
            case 'n' -> '\n';
            case 't' -> '\t';
            case 'r' -> '\r';
            case 'b' -> '\b';
            case 'f' -> '\f';
            default -> '?';
        };
    }

    @Override
    public Future<Boolean> registerHandler() {
        return executor.submit(this);
    }
}
