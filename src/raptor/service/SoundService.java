package raptor.service;

public class SoundService {

	public static final int GAME_OBS_START = 0;
	public static final int GAME_OBS_END = 1;
	public static final int GAME_OBS_MOVE = 2;
	public static final int GAME_OBS_ILLEGAL = 3;
	public static final int GAME_PLAY_START = 4;
	public static final int GAME_PLAY_END = 5;
	public static final int GAME_PLAY_MOVE = 6;
	public static final int GAME_PLAY_ILLEGAL = 7;
	public static final int TELL = 8;

	private static final SoundService instance = new SoundService();

	public static SoundService getInstance() {
		return instance;
	}

	public void play(int soundId) {
	}
}
