import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class FSMStateHandler12 implements Callable<Boolean>, IFSMStateHandler {

    private final FSMContext ctx;
    private final ExecutorService executor;

    public FSMStateHandler12(FSMContext ctx, ExecutorService executor) {
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
        return this.state12();
    }

    private boolean state12() throws IOException {
        ctx.lexer.getChar();

        if (ctx.lexer.getInputChar() == 'a') {
            ctx.nextState = 13;
            return true;
        }
        return false;
    }

    @Override
    public Future<Boolean> submitTask() {
        return executor.submit(this);
    }
}
