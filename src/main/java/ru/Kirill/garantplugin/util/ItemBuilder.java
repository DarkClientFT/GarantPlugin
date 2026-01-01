package ru.Kirill.garantplugin.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ItemBuilder {
    /// спастил с плагина какого то
    private ItemStack itemStack;
    private ItemMeta itemMeta;

    public ItemBuilder() {
        this.itemStack = new ItemStack(Material.STONE);
        this.itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
        this.itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
        this.itemMeta = this.itemStack.getItemMeta();
    }

    public ItemBuilder material(Material material) {
        this.itemStack.setType(material);
        this.itemMeta = this.itemStack.getItemMeta();
        return this;
    }

    public ItemBuilder material(String materialName) {
        try {
            Material material = Material.valueOf(materialName.toUpperCase());
            this.itemStack.setType(material);
            this.itemMeta = this.itemStack.getItemMeta();
        } catch (IllegalArgumentException ignored) {
        }
        return this;
    }

    public ItemBuilder amount(int amount) {
        this.itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilder name(String name) {
        if (this.itemMeta != null) {
            this.itemMeta.setDisplayName(ColorUtil.colorize(name));
        }
        return this;
    }

    public ItemBuilder lore(String... lore) {
        return lore(Arrays.asList(lore));
    }

    public ItemBuilder lore(List<String> lore) {
        if (this.itemMeta != null) {
            List<String> coloredLore = lore.stream()
                    .map(ColorUtil::colorize)
                    .collect(Collectors.toList());
            this.itemMeta.setLore(coloredLore);
        }
        return this;
    }

    public ItemBuilder addLore(String line) {
        if (this.itemMeta != null) {
            List<String> lore = itemMeta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.add(ColorUtil.colorize(line));
            itemMeta.setLore(lore);
        }
        return this;
    }

    public ItemBuilder addLore(List<String> lines) {
        if (this.itemMeta != null) {
            List<String> lore = itemMeta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            for (String line : lines) {
                lore.add(ColorUtil.colorize(line));
            }
            itemMeta.setLore(lore);
        }
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        if (this.itemMeta != null) {
            itemMeta.addEnchant(enchantment, level, true);
        }
        return this;
    }

    public ItemBuilder glow() {
        if (this.itemMeta != null) {
            itemMeta.addEnchant(Enchantment.LUCK, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        if (this.itemMeta != null) {
            itemMeta.addItemFlags(flags);
        }
        return this;
    }

    public ItemBuilder hideFlags() {
        if (this.itemMeta != null) {
            itemMeta.addItemFlags(ItemFlag.values());
        }
        return this;
    }

    public ItemBuilder skullOwner(UUID uuid) {
        if (this.itemMeta instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta) this.itemMeta;
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
        }
        return this;
    }

    public ItemBuilder skullOwner(String name) {
        if (this.itemMeta instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta) this.itemMeta;
            skullMeta.setOwner(name);
        }
        return this;
    }

    public ItemStack build() {
        if (this.itemMeta != null) {
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }
}