package com.aetherlia.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "team_members")
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne
    @JoinColumn(name = "player_monster_id", nullable = false)
    private PlayerMonster playerMonster;

    @Column(nullable = false)
    private int position;

    public TeamMember() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public PlayerMonster getPlayerMonster() {
        return playerMonster;
    }

    public void setPlayerMonster(PlayerMonster playerMonster) {
        this.playerMonster = playerMonster;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}