import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class FSMStateHandler1 implements Callable<Boolean>, IFSMStateHandler {

    private final FSMContext ctx;
    private final ExecutorService executor;

    public FSMStateHandler1(FSMContext ctx, ExecutorService executor) {
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
        return this.state1();
    }

    public boolean state1() {
        while (ctx.L.getChar()) {
            if (ctx.L.getInputChar() == ' ' ||
                    ctx.L.getInputChar() >= '\t' && ctx.L.getInputChar() <= '\r')
                continue;

            if (ctx.L.getInputChar() >= '1' && ctx.L.getInputChar() <= '9') {
                ctx.L.getStringBuilder().append((char) ctx.L.getInputChar());
                ctx.NextState = 3;
                return true;
            }

            switch (ctx.L.getInputChar()) {
                case '"':
                    ctx.NextState = 19;
                    ctx.Return = true;
                    return true;

                case ',':
                case ':':
                case '[':
                case ']':
                case '{':
                case '}':
                    ctx.NextState = 1;
                    ctx.Return = true;
                    return true;

                case '-':
                    ctx.L.getStringBuilder().append ((char) ctx.L.getInputChar());
                    ctx.NextState = 2;
                    return true;

                case '0':
                    ctx.L.getStringBuilder().append ((char) ctx.L.getInputChar());
                    ctx.NextState = 4;
                    return true;

                case 'f':
                    ctx.NextState = 12;
                    return true;

                case 'n':
                    ctx.NextState = 16;
                    return true;

                case 't':
                    ctx.NextState = 9;
                    return true;

                case '\'':
                    if (!ctx.L.isAllowSingleQuotedStrings())
                        return false;

                    ctx.L.setInputChar('"');
                    ctx.NextState = 23;
                    ctx.Return = true;
                    return true;

                case '/':
                    if (!ctx.L.isAllowComments())
                        return false;

                    ctx.NextState = 25;
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
