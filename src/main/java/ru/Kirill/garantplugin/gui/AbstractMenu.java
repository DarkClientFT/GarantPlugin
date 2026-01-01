package ru.Kirill.garantplugin.gui;

import ru.Kirill.garantplugin.GarantPlugin;
import ru.Kirill.garantplugin.util.ColorUtil;
import ru.Kirill.garantplugin.util.ItemBuilder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public abstract class AbstractMenu implements InventoryHolder {

    protected final GarantPlugin plugin;
    protected final Player player;
    protected Inventory inventory;
    protected final Map<Integer, Consumer<InventoryClickEvent>> clickActions;

    public AbstractMenu(GarantPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.clickActions = new HashMap<>();
    }

    protected abstract String getTitle();

    protected abstract int getSize();

    protected abstract void setupItems();

    public void open() {
        inventory = Bukkit.createInventory(this, getSize(), ColorUtil.colorize(getTitle()));
        setupItems();
        player.openInventory(inventory);
    }

    public void close() {
        player.closeInventory();
    }

    public void refresh() {
        setupItems();
    }

    protected void setItem(int slot, ItemStack item) {
        inventory.setItem(slot, item);
    }

    protected void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> action) {
        inventory.setItem(slot, item);
        clickActions.put(slot, action);
    }

    protected void fillBorder(ItemStack item) {
        int size = getSize();
        for (int i = 0; i < 9; i++) {
            setItem(i, item);
        }
        for (int i = size - 9; i < size; i++) {
            setItem(i, item);
        }
        for (int i = 9; i < size - 9; i += 9) {
            setItem(i, item);
            setItem(i + 8, item);
        }
    }

    protected void fill(ItemStack item) {
        for (int i = 0; i < getSize(); i++) {
            if (inventory.getItem(i) == null) {
                setItem(i, item);
            }
        }
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        Consumer<InventoryClickEvent> action = clickActions.get(slot);
        if (action != null) {
            action.accept(event);
        }
    }

    protected ItemBuilder itemBuilder() {
        return new ItemBuilder();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}