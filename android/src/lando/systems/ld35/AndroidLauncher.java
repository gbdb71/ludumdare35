package lando.systems.ld35;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import lando.systems.ld35.screens.AttractScreen;
import lando.systems.ld35.screens.GameScreen;
import lando.systems.ld35.screens.MenuScreen;

public class AndroidLauncher extends AndroidApplication implements ActionResolver {

	public boolean fullScreen = false;
	public boolean freePlay = false;
	public boolean showMouseCursor = false;
	public int livesPerCredit = 5;
	public int continuesPerCredit = 3;
	public int menuScreenTimer = MenuScreen.ATTRACT_TIMEOUT_SECONDS;
	public int attractScreenTimer = AttractScreen.TIMEOUT_SECONDS;
	public int limitTimer = GameScreen.LIMIT_TIMEOUT_SECONDS;
	public int warningTimer = GameScreen.WARNING_TIMEOUT_SECONDS;
	public int continueTimer = GameScreen.CONTINUE_TIMEOUT_SECONDS;
	public boolean showFPS = false;
	public boolean playMusic = true;
	public boolean showDebug = false;
	public boolean unlockAll = false;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new LudumDare35(this), config);
	}

	@Override
	public boolean isFullScreen() {
		return fullScreen;
	}

	@Override
	public boolean showMouseCursor() {
		return showMouseCursor;
	}

	@Override
	public boolean isFreePlay() {
		return freePlay;
	}

	@Override
	public int livesPerCredit() {
		return livesPerCredit;
	}

	@Override
	public int continuesPerCredit() {
		return continuesPerCredit;
	}

	@Override
	public int menuScreenTimer() {
		return menuScreenTimer;
	}

	@Override
	public int attractScreenTimer() {
		return attractScreenTimer;
	}

	@Override
	public int limitTimer() {
		return limitTimer;
	}

	@Override
	public int warningTimer() {
		return warningTimer;
	}

	@Override
	public int continueTimer() {
		return continueTimer;
	}

	@Override
	public boolean showFPS() {
		return showFPS;
	}

	@Override
	public boolean playMusic() {
		return playMusic;
	}

	@Override
	public boolean showDebug() {
		return showDebug;
	}

	@Override
	public boolean unlockAll() {
		return unlockAll;
	}

}
