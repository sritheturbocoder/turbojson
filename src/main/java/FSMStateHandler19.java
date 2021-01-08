import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class FSMStateHandler19 implements Callable<Boolean>, IFSMStateHandler {

    private FSMContext ctx;
    private ExecutorService executor;

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

    private boolean state19()
    {
        while (ctx.L.getChar()) {
            if (ctx.L.getInputChar() == '"') {
                ctx.L.ungetChar();
                ctx.Return = true;
                ctx.nextState = 20;
                return true;
            } else if (ctx.L.getInputChar() == '\\') {
                ctx.StateStack = 19;
                ctx.nextState = 21;
                return true;
            }
            ctx.L.getStringBuilder().append((char) ctx.L.getInputChar())
        }

        return true;
    }

    @Override
    public Future<Boolean> registerHandler() {
        return executor.submit(this);
    }
}
