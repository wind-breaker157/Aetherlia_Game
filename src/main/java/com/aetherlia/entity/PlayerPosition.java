package com.aetherlia.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "player_positions", uniqueConstraints = { @UniqueConstraint(columnNames = { "user_id" }) })
public class PlayerPosition {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	@ManyToOne
	@JoinColumn(name = "map_area_id", nullable = false)
	private MapArea mapArea;

	@Column(nullable = false)
	private String locationCode;

	/*
	 * Tọa độ nhân vật trong World.
	 *
	 * World: Width = 900 Height = 500
	 */
	@Column(nullable = false)
	private int x;

	@Column(nullable = false)
	private int y;

	public PlayerPosition() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public MapArea getMapArea() {
		return mapArea;
	}

	public void setMapArea(MapArea mapArea) {
		this.mapArea = mapArea;
	}

	public String getLocationCode() {
		return locationCode;
	}

	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
}