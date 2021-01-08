import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class FSMStateHandler22 implements Callable<Boolean>, IFSMStateHandler {

    private final FSMContext ctx;
    private final ExecutorService executor;

    public FSMStateHandler22(FSMContext ctx, ExecutorService executor) {
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
        return this.state22();
    }

    private boolean state22()
    {
        int counter = 0;
        int mult    = 4096;

        ctx.L.setUnichar(0);

        while (ctx.L.getChar()) {

            if (ctx.L.getInputChar() >= '0' && ctx.L.getInputChar() <= '9' ||
                    ctx.L.getInputChar() >= 'A' && ctx.L.getInputChar() <= 'F' ||
                    ctx.L.getInputChar() >= 'a' && ctx.L.getInputChar() <= 'f') {

                ctx.L.setUnichar(ctx.L.getUnichar() + getHexValue(ctx.L.getInputChar()) * mult);

                counter++;
                mult /= 16;

                if (counter == 4) {
                    ctx.L.getStringBuilder().append ((char)ctx.L.getUnichar());
                    ctx.nextState = ctx.StateStack;
                    return true;
                }
                continue;
            }

            return false;
        }

        return true;
    }

    private static int getHexValue(int digit)
    {
        return switch (digit) {
            case 'a', 'A' -> 10;
            case 'b', 'B' -> 11;
            case 'c', 'C' -> 12;
            case 'd', 'D' -> 13;
            case 'e', 'E' -> 14;
            case 'f', 'F' -> 15;
            default -> digit - '0';
        };
    }

    @Override
    public Future<Boolean> registerHandler() {
        return executor.submit(this);
    }
}
