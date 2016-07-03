package vg.civcraft.mc.namelayer.permission;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PlayerType {

	private String name;
	private int id;
	private List<PlayerType> children;
	private PlayerType parent;
	private List<PermissionType> perms;

	/**
	 * For creating completly new types
	 */
	public PlayerType(String name, int id, PlayerType parent) {
		this.name = name;
		this.parent = parent;
		this.id = id;
		this.children = new LinkedList<PlayerType>();
		if (parent != null) {
			// flat copy perms
			this.perms = new LinkedList<PermissionType>(parent.perms);
			parent.addChild(this);
		}
		else {
			for(PermissionType perm : PermissionType.getAllPermissions()) {
				//new root with all permissions
				perms.add(perm);
			}
		}
	}
	
	/**
	 * For loading existing types
	 */
	public PlayerType(String name, int id, PlayerType parent, List <PermissionType> perms) {
		this.name = name;
		this.parent = parent;
		this.id = id;
		this.children = new LinkedList<PlayerType>();
		this.perms = perms;
		if (parent != null) {
			parent.addChild(this);
		}
	}

	public PlayerType getParent() {
		return parent;
	}

	public String getName() {
		return name;
	}

	public List<PlayerType> getChildren() {
		// dont let them change the list itself
		return new ArrayList<PlayerType>(children);
	}

	public boolean isChildren(PlayerType type) {
		return children.contains(type);
	}

	public boolean addChild(PlayerType child) {
		children.add(child);
		return true;
	}

	public boolean removeChild(PlayerType child) {
		if (isChildren(child)) {
			children.remove(child);
			return true;
		} else {
			return false;
		}
	}

	public boolean addPermission(PermissionType perm) {
		if (parent == null) {
			// is root and shouldnt be modified
			return false;
		}
		if (perms.contains(perm)) {
			// already exists
			return false;
		}
		if (!parent.hasPermission(perm)) {
			// would create inconsistent perm structure
			return false;
		}
		perms.add(perm);
		// TODO save to db and update cache on other shards
		return true;
	}

	public List<PlayerType> getRecursiveChildren() {
		List<PlayerType> types = new LinkedList<PlayerType>();
		for (PlayerType child : children) {
			types.add(child);
			types.addAll(child.getRecursiveChildren());
		}
		return types;
	}

	public boolean removePermission(PermissionType perm) {
		if (parent == null) {
			// is root and shouldnt be modified
			return false;
		}
		if (!perms.contains(perm)) {
			// doesn't exists
			return false;
		}
		perms.remove(perm);
		// enforce consistent structure by updating children
		for (PlayerType type : getRecursiveChildren()) {
			if (type.hasPermission(perm)) {
				type.removePermission(perm);
			}
		}
		// TODO save to db and update cache on other shards
		return true;
	}

	public boolean hasPermission(PermissionType perm) {
		return perms.contains(perm);
	}
	
	public List <PermissionType> getAllPermissions() {
		return new LinkedList<PermissionType>(perms);
	}

	public int getId() {
		return id;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof PlayerType)) {
			return false;
		}
		PlayerType comp = (PlayerType) o;
		return comp.getId() == this.getId() && comp.getName().equals(this.getName());
	}

}
