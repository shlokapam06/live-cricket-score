package com.cricketscore.cricket_score.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "teams", uniqueConstraints = {
		@UniqueConstraint(columnNames = "name"),
		@UniqueConstraint(columnNames = "short_name")
})
@Getter
@Setter
@NoArgsConstructor
public class Team {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	private String name;

	@NotBlank
	private String shortName;

	public Team(String name, String shortName) {
		this.name = name;
		this.shortName = shortName;
	}
}