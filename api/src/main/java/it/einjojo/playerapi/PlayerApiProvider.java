package it.einjojo.playerapi;

/**
 * Holds an instance of {@link PlayerApi} which can be obtained with {@link PlayerApiProvider#getInstance()}.
 */
public class PlayerApiProvider {

    private static PlayerApi instance;

    private PlayerApiProvider() {

    }


    static void register(PlayerApi playerApi) {
        if (instance != null) {
            throw new IllegalStateException("PlayerApi is already registered!");
        }
        instance = playerApi;
    }

    /**
     * getter
     *
     * @return instance of {@link PlayerApi}
     * @throws IllegalStateException if {@link PlayerApi} is not registered yet.
     *
     */
    public static PlayerApi getInstance() {
        if (instance == null) {
            throw new IllegalStateException("PlayerApi is not registered yet!");
        }
        return instance;
    }

}
