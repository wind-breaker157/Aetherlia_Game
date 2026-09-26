package com.aetherlia.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "monsters")
public class Monster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String type;

	@Column(nullable = false)
	private int baseHp;

	@Column(nullable = false)
	private int baseAttack;

	@Column(nullable = false)
	private int baseDefense;

	@Column(nullable = false)
	private int baseSpeed;

	/*
	 * Common / Uncommon / Rare / Epic / Legendary
	 */
	@Column(nullable = false)
	private String rarity = "Common";

	/*
	 * Catch rate riêng của loài. GDD có trường này trong Monster schema.
	 */
	@Column(nullable = false)
	private double catchRate = 1.0;

	/*
	 * EXP cơ bản nhận được khi đánh bại Monster này.
	 */
	@Column(nullable = false)
	private int baseExp = 50;

// =====================================================
// CONSTRUCTOR
// =====================================================

	public Monster() {
	}

// =====================================================
// GETTER / SETTER - ID
// =====================================================

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

// =====================================================
// GETTER / SETTER - NAME
// =====================================================

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

// =====================================================
// GETTER / SETTER - TYPE
// =====================================================

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

// =====================================================
// GETTER / SETTER - BASE HP
// =====================================================

	public int getBaseHp() {
		return baseHp;
	}

	public void setBaseHp(int baseHp) {
		this.baseHp = baseHp;
	}

// =====================================================
// GETTER / SETTER - BASE ATTACK
// =====================================================

	public int getBaseAttack() {
		return baseAttack;
	}

	public void setBaseAttack(int baseAttack) {
		this.baseAttack = baseAttack;
	}

// =====================================================
// GETTER / SETTER - BASE DEFENSE
// =====================================================

	public int getBaseDefense() {
		return baseDefense;
	}

	public void setBaseDefense(int baseDefense) {
		this.baseDefense = baseDefense;
	}

// =====================================================
// GETTER / SETTER - BASE SPEED
// =====================================================

	public int getBaseSpeed() {
		return baseSpeed;
	}

	public void setBaseSpeed(int baseSpeed) {
		this.baseSpeed = baseSpeed;
	}

// =====================================================
// GETTER / SETTER - RARITY
// =====================================================

	public String getRarity() {
		return rarity;
	}

	public void setRarity(String rarity) {
		this.rarity = rarity;
	}

// =====================================================
// GETTER / SETTER - CATCH RATE
// =====================================================

	public double getCatchRate() {
		return catchRate;
	}

	public void setCatchRate(double catchRate) {
		this.catchRate = catchRate;
	}

// =====================================================
// GETTER / SETTER - BASE EXP
// =====================================================

	public int getBaseExp() {
		return baseExp;
	}

	public void setBaseExp(int baseExp) {
		this.baseExp = baseExp;
	}

}
