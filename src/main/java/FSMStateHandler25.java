import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class FSMStateHandler25 implements Callable<Boolean>, IFSMStateHandler {

    private final FSMContext ctx;
    private final ExecutorService executor;

    public FSMStateHandler25(FSMContext ctx, ExecutorService executor) {
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
        return this.state25();
    }

    private boolean state25()
    {
        ctx.L.getChar();

        switch (ctx.L.getInputChar()) {
            case '*':
                ctx.nextState = 27;
                return true;

            case '/':
                ctx.nextState = 26;
                return true;

            default:
                return false;
        }
    }



    @Override
    public Future<Boolean> registerHandler() {
        return executor.submit(this);
    }
}
