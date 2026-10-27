package com.example.axefortune;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** config menu kinda */
public class AxeFortuneScreen extends Screen {
    private static final String[] ROMAN = {"I", "II", "III", "IV", "V", "VI"};

    private final Screen parent;
    private final List<EditBox> boxes = new ArrayList<>();

    public AxeFortuneScreen(Screen parent) {
        super(Component.literal("Axe Fortune"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        boxes.clear();
        int[][] table = AxeFortune.getRows();
        int count = Math.max(3, Math.min(table.length, ROMAN.length));
        int cx = this.width / 2;

        StringWidget title = new StringWidget(this.title, this.font);
        title.setPosition(cx - title.getWidth() / 2, 16);
        this.addRenderableWidget(title);

        StringWidget help = new StringWidget(
            Component.literal("Weights for getting 1, 2, 3... logs, e.g. 20,80 (shares, any total)"), this.font);
        help.setPosition(cx - help.getWidth() / 2, 32);
        this.addRenderableWidget(help);

        int y = 56;
        for (int n = 0; n < count; n++) {
            StringWidget label = new StringWidget(Component.literal("Fortune " + ROMAN[n]), this.font);
            label.setPosition(cx - 150, y + 6);
            this.addRenderableWidget(label);

            EditBox box = new EditBox(this.font, cx - 60, y, 210, 20, Component.literal("Fortune " + ROMAN[n]));
            box.setMaxLength(64);
            box.setValue(n < table.length ? join(table[n]) : join(table[table.length - 1]));
            this.addRenderableWidget(box);
            boxes.add(box);
            y += 28;
        }

        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> save())
            .bounds(cx - 154, y + 12, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Reset to defaults"), b -> resetDefaults())
            .bounds(cx - 50, y + 12, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> onClose())
            .bounds(cx + 54, y + 12, 100, 20).build());
    }

    private void save() {
        int[][] result = new int[boxes.size()][];
        boolean ok = true;
        for (int i = 0; i < boxes.size(); i++) {
            int[] row = AxeFortune.parseWeights(boxes.get(i).getValue());
            if (row == null) {
                boxes.get(i).setTextColor(0xFF5555);
                ok = false;
            } else {
                boxes.get(i).setTextColor(0xE0E0E0);
                result[i] = row;
            }
        }
        if (!ok) return; // stay open, invalid boxes are red
        AxeFortune.saveRows(result);
        this.minecraft.setScreenAndShow(parent);
    }

    private void resetDefaults() {
        String[] defaults = {"20,80", "30,25,45", "20,20,20,40"};
        for (int i = 0; i < boxes.size(); i++) {
            boxes.get(i).setTextColor(0xE0E0E0);
            boxes.get(i).setValue(i < defaults.length ? defaults[i] : defaults[defaults.length - 1]);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(parent);
    }

    private static String join(int[] row) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < row.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(row[i]);
        }
        return sb.toString();
    }
}
