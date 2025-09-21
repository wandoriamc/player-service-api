package it.einjojo.playerapi;

import io.grpc.ManagedChannel;
import it.einjojo.playerapi.impl.AbstractPlayerApi;

import java.util.concurrent.Executor;

public class PaperPlayerApi extends AbstractPlayerApi {
    private final LocalOnlinePlayerAccessor localOnlinePlayerAccessor;

    /**
     * Constructor for AbstractPlayerApi.
     *
     * @param channel  the gRPC channel to communicate with the player service
     * @param executor the executor to run the callbacks on
     */
    public PaperPlayerApi(ManagedChannel channel, Executor executor) {
        super(channel, executor);
        this.localOnlinePlayerAccessor = new PaperLocalPlayerAccessor();
    }

    @Override
    public LocalOnlinePlayerAccessor getLocalOnlinePlayerAccessor() {
        return localOnlinePlayerAccessor;
    }


}
