package com.aetherlia.battle;

public class BattleSkill {

	private String id;
	private String name;
	private String type;
	private int power;
	private int accuracy;
	private int priority;

// =====================================================
// CONSTRUCTOR CŨ
// =====================================================

	public BattleSkill(String id, String name, String type, int power, int accuracy) {

		this(id, name, type, power, accuracy, 0);
	}

// =====================================================
// CONSTRUCTOR CÓ PRIORITY
// =====================================================

	public BattleSkill(String id, String name, String type, int power, int accuracy, int priority) {

		this.id = id;
		this.name = name;
		this.type = type;
		this.power = power;
		this.accuracy = accuracy;
		this.priority = priority;
	}

// =====================================================
// GETTER
// =====================================================

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public int getPower() {
		return power;
	}

	public int getAccuracy() {
		return accuracy;
	}

	public int getPriority() {
		return priority;
	}

// =====================================================
// SETTER
// =====================================================

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setPower(int power) {
		this.power = power;
	}

	public void setAccuracy(int accuracy) {
		this.accuracy = accuracy;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

}
