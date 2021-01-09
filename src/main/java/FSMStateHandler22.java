import java.io.IOException;
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

    private static int getHexValue(int digit) {
        //noinspection EnhancedSwitchMigration
        switch (digit) {
            case 'A' :
                return  10;
            case 'a':
                return  10;
            case 'b':
                return 11;
            case 'B':
                return 11;
            case 'c':
                return 12;
            case 'C':
                return 12;
            case 'd':
                return 13;
            case 'D':
                return 13;
            case 'e':
                return 14;
            case 'E':
                return 14;
            case 'f':
                return 15;
            case 'F':
                return 15;
            default :
                return digit - '0';
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
        return this.state22();
    }

    private boolean state22() throws IOException {
        int counter = 0;
        int mult = 4096;

        ctx.lexer.setUnichar(0);

        while (ctx.lexer.getChar()) {

            if (ctx.lexer.getInputChar() >= '0' && ctx.lexer.getInputChar() <= '9' ||
                    ctx.lexer.getInputChar() >= 'A' && ctx.lexer.getInputChar() <= 'F' ||
                    ctx.lexer.getInputChar() >= 'a' && ctx.lexer.getInputChar() <= 'f') {

                ctx.lexer.setUnichar(ctx.lexer.getUnichar() + getHexValue(ctx.lexer.getInputChar()) * mult);

                counter++;
                mult /= 16;

                if (counter == 4) {
                    ctx.lexer.getStringBuilder().append((char) ctx.lexer.getUnichar());
                    ctx.nextState = ctx.stateStack;
                    return true;
                }
                continue;
            }

            return false;
        }

        return true;
    }

    @Override
    public Future<Boolean> submitTask() {
        return executor.submit(this);
    }
}
