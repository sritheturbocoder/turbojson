import java.util.concurrent.Future;

interface IFSMStateHandler {
    Future<Boolean> registerHandler();
}
