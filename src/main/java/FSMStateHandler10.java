import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class FSMStateHandler10 implements Callable<Boolean>, IFSMStateHandler {

    private final FSMContext ctx;
    private final ExecutorService executor;

    public FSMStateHandler10(FSMContext ctx, ExecutorService executor) {
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
        return this.state10();
    }

    private boolean state10 ()
    {
        ctx.L.getChar();

        if (ctx.L.getInputChar() == 'u') {
            ctx.nextState = 11;
            return true;
        }
        return false;
    }

    @Override
    public Future<Boolean> registerHandler() {
        return executor.submit(this);
    }
}
