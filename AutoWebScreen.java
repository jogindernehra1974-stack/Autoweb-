package com.autoweb.client.gui;

import com.autoweb.client.config.ConfigManager;
import com.autoweb.client.module.AutowebModule;
import com.autoweb.client.module.BoolSetting;
import com.autoweb.client.module.DoubleSetting;
import com.autoweb.client.module.EnumSetting;
import com.autoweb.client.module.ModuleManager;
import com.autoweb.client.module.Setting;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

@Environment(value=EnvType.CLIENT)
public class AutoWebScreen extends Screen {
    private static final int PANEL_W = 280;
    private static final int PANEL_X = 10;
    private static final int PANEL_Y = 10;
    private static final int ROW_H = 22;
    private static final int SET_ROW_H = 18;
    private static final int TITLE_H = 16;
    private static final int COL_BG = -870704594;
    private static final int COL_HEADER = -15326914;
    private static final int COL_ROW = -15780768;
    private static final int COL_ROW_ALT = -15326137;
    private static final int COL_ON = -16725866;
    private static final int COL_OFF = -11184811;
    private static final int COL_BTN = -14013878;
    private static final int COL_BTN_HL = -12961174;
    private static final int COL_CAPTURE = -1489568;
    private static final int COL_TEXT = -1;
    private static final int COL_SUBTEXT = -5592406;
    private static final int COL_SLIDER_BG = -13421739;
    private static final int COL_SLIDER_FG = -11175937;
    private static final int COL_FRIEND_ROW = -15916745;
    private final List<AutowebModule> mods = ModuleManager.getModules();
    private int expandedIndex = -1;
    private AutowebModule capturingModule = null;
    private String friendInputText = "";
    private boolean friendInputActive = false;
    private int friendScrollOffset = 0;

    public AutoWebScreen() {
        super(Text.literal("AutoWeb"));
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, this.width, this.height, -2013265920);
        int py = 10;
        ctx.fill(10, py, 290, py + 16, COL_HEADER);
        ctx.drawText(this.textRenderer, "\u00a7bAutoWeb \u00a77Modules", 15, py + 4, COL_TEXT, false);
        py += 18;
        for (int i = 0; i < this.mods.size(); ++i) {
            AutowebModule m = this.mods.get(i);
            int rowBg = i % 2 == 0 ? COL_ROW : COL_ROW_ALT;
            ctx.fill(10, py, 290, py + 22, rowBg);
            ctx.drawText(this.textRenderer, m.getName(), 15, py + 7, COL_TEXT, false);
            int togX = 160;
            int togColor = m.isEnabled() ? COL_ON : COL_OFF;
            ctx.fill(togX, py + 4, togX + 34, py + 18, togColor);
            ctx.drawText(this.textRenderer, m.isEnabled() ? "\u00a7aON" : "\u00a77OFF", togX + 5, py + 7, COL_TEXT, false);
            int setX = togX + 38;
            boolean expanded = this.expandedIndex == i;
            int setBg = expanded ? COL_BTN_HL : COL_BTN;
            ctx.fill(setX, py + 4, setX + 18, py + 18, setBg);
            ctx.drawText(this.textRenderer, "\u00a7e\u2699", setX + 4, py + 7, COL_TEXT, false);
            int bindX = setX + 22;
            boolean isCapturing = this.capturingModule == m;
            int bindBg = isCapturing ? COL_CAPTURE : COL_BTN;
            ctx.fill(bindX, py + 4, bindX + 54, py + 18, bindBg);
            String keyLabel = isCapturing ? "..." : AutowebModule.keyName(m.getBoundKeyCode());
            ctx.drawText(this.textRenderer, keyLabel, bindX + 3, py + 7, COL_TEXT, false);
            py += 22;
            if (!expanded) continue;
            for (Setting<?> s : m.getSettings()) {
                py = this.renderSetting(ctx, s, py, mx, my);
            }
        }
        py += 4;
        ctx.fill(10, py, 290, py + 16, COL_HEADER);
        ctx.drawText(this.textRenderer, "\u00a7dFriendlist", 15, py + 4, COL_TEXT, false);
        py += 16;
        List<String> friends = ConfigManager.getFriendlist();
        int visibleFriends = Math.min(friends.size(), 5);
        for (int fi = this.friendScrollOffset; fi < Math.min(this.friendScrollOffset + visibleFriends, friends.size()); ++fi) {
            String f = friends.get(fi);
            ctx.fill(10, py, 290, py + 22, COL_FRIEND_ROW);
            ctx.drawText(this.textRenderer, f, 15, py + 7, COL_SUBTEXT, false);
            int remX = 260;
            ctx.fill(remX, py + 4, remX + 24, py + 18, -7667712);
            ctx.drawText(this.textRenderer, "\u00a7cRem", remX + 2, py + 7, COL_TEXT, false);
            py += 22;
        }
        py += 2;
        ctx.fill(10, py, 250, py + 22, this.friendInputActive ? -14535851 : COL_BTN);
        String displayInput = this.friendInputText.isEmpty() ? "\u00a77type name..." : this.friendInputText;
        ctx.drawText(this.textRenderer, displayInput, 14, py + 7, COL_TEXT, false);
        int addX = 254;
        ctx.fill(addX, py, 290, py + 22, -16751053);
        ctx.drawText(this.textRenderer, "\u00a7aAdd", addX + 4, py + 7, COL_TEXT, false);
        super.render(ctx, mx, my, delta);
    }

    private int renderSetting(DrawContext ctx, Setting<?> s, int py, int mx, int my) {
        ctx.fill(18, py, 282, py + 18, -16119265);
        ctx.drawText(this.textRenderer, "\u00a77" + s.getName(), 22, py + 4, COL_SUBTEXT, false);
        if (s instanceof DoubleSetting ds) {
            int slX = 100;
            int slW = 180;
            int slY = py + 5;
            int slH = 8;
            ctx.fill(slX, slY, slX + slW, slY + slH, COL_SLIDER_BG);
            int fillW = (int)(ds.getSliderFraction() * (double)slW);
            ctx.fill(slX, slY, slX + fillW, slY + slH, COL_SLIDER_FG);
            String valStr = String.format("%.1f", ds.getValue());
            ctx.drawText(this.textRenderer, valStr, slX + slW + 3, py + 4, COL_TEXT, false);
        } else if (s instanceof BoolSetting bs) {
            int bX = 250;
            ctx.fill(bX, py + 3, bX + 30, py + 15, (Boolean)bs.getValue() ? COL_ON : COL_OFF);
            ctx.drawText(this.textRenderer, (Boolean)bs.getValue() ? "\u00a7aON" : "\u00a77OFF", bX + 4, py + 5, COL_TEXT, false);
        } else if (s instanceof EnumSetting es) {
            int eX = 210;
            ctx.fill(eX, py + 3, eX + 72, py + 15, COL_BTN);
            ctx.drawText(this.textRenderer, (String)es.getValue(), eX + 4, py + 5, COL_TEXT, false);
        }
        return py + 18;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        int py = 28;
        for (int i = 0; i < this.mods.size(); ++i) {
            AutowebModule m = this.mods.get(i);
            if (mouseY >= (double)py && mouseY < (double)(py + 22)) {
                int togX = 160;
                int setX = togX + 38;
                int bindX = setX + 22;
                if (mouseX >= (double)togX && mouseX < (double)(togX + 34)) {
                    m.toggle();
                    return true;
                }
                if (mouseX >= (double)setX && mouseX < (double)(setX + 18)) {
                    this.expandedIndex = this.expandedIndex == i ? -1 : i;
                    return true;
                }
                if (mouseX >= (double)bindX && mouseX < (double)(bindX + 54)) {
                    this.capturingModule = m;
                    return true;
                }
            }
            py += 22;
            if (this.expandedIndex != i) continue;
            for (Setting<?> s : m.getSettings()) {
                if (mouseY >= (double)py && mouseY < (double)(py + 18)) {
                    this.handleSettingClick(s, mouseX, py, button);
                    return true;
                }
                py += 18;
            }
        }
        py += 20;
        List<String> friends = ConfigManager.getFriendlist();
        int visibleFriends = Math.min(friends.size(), 5);
        for (int fi = this.friendScrollOffset; fi < Math.min(this.friendScrollOffset + visibleFriends, friends.size()); ++fi) {
            if (mouseY >= (double)py && mouseY < (double)(py + 22)) {
                int remX = 260;
                if (mouseX >= (double)remX && mouseX < (double)(remX + 24)) {
                    ConfigManager.removeFriend(friends.get(fi));
                    return true;
                }
            }
            py += 22;
        }
        py += 2;
        if (mouseY >= (double)py && mouseY < (double)(py + 22)) {
            int addX = 254;
            if (mouseX >= (double)addX && mouseX < 290.0) {
                if (!this.friendInputText.isBlank()) {
                    ConfigManager.addFriend(this.friendInputText.trim());
                    this.friendInputText = "";
                }
            } else {
                this.friendInputActive = !this.friendInputActive;
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleSettingClick(Setting<?> s, double mx, int py, int button) {
        if (s instanceof BoolSetting bs) {
            int bX = 250;
            if (mx >= (double)bX && mx < (double)(bX + 30)) {
                bs.toggle();
                ConfigManager.save();
            }
        } else if (s instanceof EnumSetting es) {
            int eX = 210;
            if (mx >= (double)eX && mx < (double)(eX + 72)) {
                es.cycleNext();
                ConfigManager.save();
            }
        } else if (s instanceof DoubleSetting ds) {
            int slX = 100;
            int slW = 180;
            if (mx >= (double)slX && mx < (double)(slX + slW)) {
                double fraction = (mx - (double)slX) / (double)slW;
                ds.setFromFraction(fraction);
                ConfigManager.save();
            }
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button != 0) return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        int py = 28;
        for (int i = 0; i < this.mods.size(); ++i) {
            py += 22;
            if (this.expandedIndex != i) continue;
            for (Setting<?> s : this.mods.get(i).getSettings()) {
                if (s instanceof DoubleSetting ds && mouseY >= (double)py && mouseY < (double)(py + 18)) {
                    int slX = 100;
                    int slW = 180;
                    double fraction = Math.max(0.0, Math.min(1.0, (mouseX - (double)slX) / (double)slW));
                    ds.setFromFraction(fraction);
                    ConfigManager.save();
                    return true;
                }
                py += 18;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.capturingModule != null) {
            this.capturingModule.setKeyCode(keyCode == 256 ? -1 : keyCode);
            this.capturingModule = null;
            return true;
        }
        if (keyCode == 256 || keyCode == 79) {
            this.close();
            return true;
        }
        if (this.friendInputActive) {
            if (keyCode == 259 && !this.friendInputText.isEmpty()) {
                this.friendInputText = this.friendInputText.substring(0, this.friendInputText.length() - 1);
                return true;
            }
            if (keyCode == 257) {
                if (!this.friendInputText.isBlank()) {
                    ConfigManager.addFriend(this.friendInputText.trim());
                    this.friendInputText = "";
                }
                this.friendInputActive = false;
                return true;
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.capturingModule != null) return true;
        if (this.friendInputActive) {
            int cp = (int) chr;
            if (cp >= 32 && cp < 127) {
                this.friendInputText = this.friendInputText + chr;
            }
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int totalFriends = ConfigManager.getFriendlist().size();
        if (verticalAmount < 0.0) {
            this.friendScrollOffset = Math.min(this.friendScrollOffset + 1, Math.max(0, totalFriends - 5));
        } else {
            this.friendScrollOffset = Math.max(0, this.friendScrollOffset - 1);
        }
        return true;
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public boolean shouldCloseOnEsc() { return true; }
}
