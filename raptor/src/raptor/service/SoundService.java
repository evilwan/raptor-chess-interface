package raptor.service;

public class SoundService {

	public static final int GAME_OBS_START = 0;
	public static final int GAME_OBS_END = 1;
	public static final int GAME_OBS_MOVE = 2;
	public static final int GAME_PLAY_START = 3;
	public static final int GAME_PLAY_END = 4;
	public static final int GAME_PLAY_MOVE = 5;
	public static final int TELL = 6;

	private static final SoundService instance = new SoundService();

	public static SoundService getInstance() {
		return instance;
	}

	public void play(int soundId) {
	}
}
