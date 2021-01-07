import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class FSMStateHandler6 implements Callable<Boolean>, IFSMStateHandler {

    private FSMContext ctx;
    private ExecutorService executor;

    public FSMStateHandler6(FSMContext ctx, ExecutorService executor) {
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
        return this.state6();
    }

    private boolean state6()
    {
        while (ctx.L.getChar()) {
            if (ctx.L.getInputChar() >= '0' && ctx.L.getInputChar() <= '9') {
                ctx.L.getStringBuilder().append((char) ctx.L.getInputChar());
                continue;
            }

            if (ctx.L.getInputChar() == ' ' ||
                    ctx.L.getInputChar() >= '\t' && ctx.L.getInputChar() <= '\r') {
                ctx.Return = true;
                ctx.NextState = 1;
                return true;
            }

            switch (ctx.L.getInputChar()) {
                case ',':
                case ']':
                case '}':
                    ctx.L.ungetChar();
                    ctx.Return = true;
                    ctx.NextState = 1;
                    return true;

                case 'e':
                case 'E':
                    ctx.L.getStringBuilder().append((char) ctx.L.getInputChar());
                    ctx.NextState = 7;
                    return true;

                default:
                    return false;
            }
        }

        return true;
    }

    @Override
    public Future<Boolean> registerHandler() {
        return executor.submit(this);
    }
}
