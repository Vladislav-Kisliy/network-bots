package com.karlsoft.network.telnet.protocol.setting;

import com.karlsoft.network.telnet.protocol.TelnetOption;

public class WindowSizeSetting extends AbstractTelnetSetting {

    private static final int DEFAULT_WIDTH = 80;
    private static final int DEFAULT_HEIGHT = 24;

    private final int width;
    private final int height;

    public WindowSizeSetting() {
        super(TelnetOption.WINDOW_SIZE);
        this.width = DEFAULT_WIDTH;
        this.height = DEFAULT_HEIGHT;
    }

    public WindowSizeSetting(int width, int height) {
        super(TelnetOption.WINDOW_SIZE);
        this.width = width;
        this.height = height;
    }

    public WindowSizeSetting(int width, int height,
                             boolean initialLocal, boolean initialRemote, boolean acceptLocal, boolean acceptRemote) {
        super(TelnetOption.WINDOW_SIZE, initialLocal, initialRemote, acceptLocal, acceptRemote);
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
