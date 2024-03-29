package NextBlock;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class CustomItems {
	//public ItemStack baseGen, exbaseGen;
	public ItemStack gen1x1, gen3x3, gen5x5, gen15x15, exGen3x3, exGen5x5, exGen9x9, exGen15x15;
	public String baseHead, exbaseHead;
	public String head1x1, head3x3, head5x5, head15x15, exHead3x3, exHead5x5, exHead9x9, exHead15x15;
	public ArrayList<ItemStack> Gens = new ArrayList<>();

	public CustomItems() {
		gen3x3 = new ItemStack(Material.PLAYER_HEAD, 1);
		gen1x1= gen3x3.clone();
		gen5x5 = gen3x3.clone();
		gen15x15 = gen3x3.clone();
		exGen3x3 = gen3x3.clone();
		exGen5x5 = gen3x3.clone();
		exGen9x9 = gen3x3.clone();
		exGen15x15 = gen3x3.clone();
		//baseGen = new ItemStack(Material.STRUCTURE_BLOCK, 1);
		//exbaseGen = baseGen.clone();
		CreateItems();
	}
	
	public ItemStack getPlayerHead(String playerName) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
        head.setItemMeta(meta);
        return head;
    }

	public void ReLoadHead() {
			gen1x1 = getPlayerHead(head1x1);
			gen3x3 = getPlayerHead(head3x3);
			gen5x5 = getPlayerHead(head5x5);
			gen15x15 = getPlayerHead(head15x15);
			exGen3x3 = getPlayerHead(exHead3x3);
			exGen5x5 = getPlayerHead(exHead5x5);
			exGen9x9 = getPlayerHead(exHead9x9);
			exGen15x15 = getPlayerHead(exHead15x15);
			//baseGen = api.getItemHead(baseHead);
			//exbaseGen = api.getItemHead(exbaseHead);
			CreateItems();
	}

	private void SetParameters(ItemStack item, String name, ArrayList<String> lore) {
		ItemMeta meta = item.getItemMeta();
		meta.setUnbreakable(true);
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
		meta.setDisplayName(name);
		meta.setLore(lore);
		item.setItemMeta(meta);
	}

	private void CreateItems() {
		ArrayList<String> Lore = new ArrayList<>();
		Lore.add("Territory Generator");
		Lore.add("3x3 generation radius");
		SetParameters(gen3x3, "3x3 Generator", Lore);

		Lore.set(1, "1x1 generation radius");
		SetParameters(gen1x1, "Simple Generator", Lore);

		Lore.set(1, "5x5 generation radius");
		SetParameters(gen5x5, "5x5 Generator", Lore);

		Lore.set(1, "Generation radius 15x15");
		SetParameters(gen15x15, "Generator 15x15", Lore);

		Lore.set(1, "3x3 but fast");
		SetParameters(exGen3x3, "Improved 3x3 Generator", Lore);

		Lore.set(1, "5x5 but fast");
		SetParameters(exGen5x5, "Improved 5x5 Generator", Lore);

		Lore.set(1, "9x9 but fast");
		SetParameters(exGen9x9, "Improved 9x9 Generator", Lore);

		Lore.set(1, "15x15 but fast");
		SetParameters(exGen15x15, "Improved Generator 15x15", Lore);

		/*
		 * Lore.set(0, "Block generator..."); Lore.set(1,
		 * "Infinite, but not too efficient"); SetParameters(baseGen, "Basic", Lore);
		 * 
		 * SetParameters(exbaseGen, "Basic++", Lore);
		 */

		Gens.clear();
		Gens.add(gen1x1);
		Gens.add(gen3x3);
		Gens.add(gen5x5);
		Gens.add(gen15x15);
		Gens.add(exGen3x3);
		Gens.add(exGen5x5);
		Gens.add(exGen9x9);
		Gens.add(exGen15x15);
		//Gens.add(baseGen);
		//Gens.add(exbaseGen);
	}
}