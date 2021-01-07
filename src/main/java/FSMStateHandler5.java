import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class FSMStateHandler5 implements Callable<Boolean>, IFSMStateHandler {

    private FSMContext ctx;
    private ExecutorService executor;

    public FSMStateHandler5(FSMContext ctx, ExecutorService executor) {
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
        return this.state5();
    }

    private boolean state5()
    {
        ctx.L.getChar();

        if (ctx.L.getInputChar() >= '0' && ctx.L.getInputChar() <= '9') {
            ctx.L.getStringBuilder().append ((char) ctx.L.getInputChar());
            ctx.NextState = 6;
            return true;
        }

        return false;
    }

    @Override
    public Future<Boolean> registerHandler() {
        return executor.submit(this);
    }
}
