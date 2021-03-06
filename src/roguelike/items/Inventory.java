package roguelike.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import roguelike.mob.BaseEntity;

public class Inventory {
	private List <Item> inventory;
	private double currentWeight;
	private BaseEntity owner;
	
	public Inventory(BaseEntity owner){
		inventory = new ArrayList <> ();
		this.owner = owner;
	}
	
	public double CurrentWeight(){
		return currentWeight;
	}
	
	public List<Item> getItems(){ return this.inventory; }
	
	public void add(Item item){
		if(owner.currentCarryWeight() + item.getWeight() <= owner.getMaxCarryWeight()){
			inventory.add(item);
            currentWeight += item.getWeight();
		}
	}

	public int size(){ return inventory.size(); }

	public void remove(Item item){
		if(inventory.contains(item)){
			inventory.remove(item);
		}
		else{
			owner.notify("Remove what?");
		}
		currentWeight -= item.getWeight();
	}
	
	public boolean contains(Item item){
		for(Item otherItem : inventory){
			if(item.equals(otherItem)){
				return true;
			}
		}
		return false;
	}
}
