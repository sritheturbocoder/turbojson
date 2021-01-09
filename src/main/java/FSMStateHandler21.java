import java.io.IOException;
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

    private static char processEscChar(int esc_char) {
        //noinspection EnhancedSwitchMigration
        switch (esc_char) {
            case '"', '\'', '\\', '/' : return (char) esc_char;
            case 'n' : return '\n';
            case 't' : return '\t';
            case 'r' : return '\r';
            case 'b' : return '\b';
            case 'f' : return '\f';
            default : return '?';
        }
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

    private boolean state21() throws IOException {
        ctx.lexer.getChar();

        switch (ctx.lexer.getInputChar()) {
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
                ctx.lexer.getStringBuilder().append(
                        processEscChar(ctx.lexer.getInputChar()));
                ctx.nextState = ctx.stateStack;
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
