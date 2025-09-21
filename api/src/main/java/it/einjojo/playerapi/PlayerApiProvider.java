package it.einjojo.playerapi;

public class PlayerApiProvider {

    private static PlayerApi instance;

    private PlayerApiProvider() {

    }

    static void register(PlayerApi playerApi) {
        if (instance != null) {
            throw new IllegalStateException("PlayerApi is already registered!" );
        }
        instance = playerApi;
    }

    public static PlayerApi getInstance() {
        if (instance == null) {
            throw new IllegalStateException("PlayerApi is not registered yet!" );
        }
        return instance;
    }

}
