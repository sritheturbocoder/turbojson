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

    private boolean state23()
    {
        while (ctx.L.getChar()) {
            switch (ctx.L.getInputChar()) {
                case '\'' -> {
                    ctx.L.ungetChar();
                    ctx.Return = true;
                    ctx.nextState = 24;
                    return true;
                }
                case '\\' -> {
                    ctx.StateStack = 23;
                    ctx.nextState = 21;
                    return true;
                }
                default -> ctx.L.getStringBuilder().append((char) ctx.L.getInputChar());
            }
        }

        return true;
    }

    @Override
    public Future<Boolean> registerHandler() {
        return executor.submit(this);
    }
}
