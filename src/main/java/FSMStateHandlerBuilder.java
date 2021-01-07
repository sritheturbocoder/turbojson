public class FSMStateHandlerBuilder {

    private IFSMStateHandler state;

    public FSMStateHandlerBuilder() {

    }

    public FSMStateHandlerBuilder with(IFSMStateHandler state) {
        this.state = state;
        return this;
    }


    public IFSMStateHandler build() {
        return this.state;
    }
}
