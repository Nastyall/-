package com.example.restaurantrating.model;

import lombok.Getter;

@Getter
public enum CuisineType {
    ITALIAN("Итальянская"),
    CHINESE("Китайская"),
    JAPANESE("Японская"),
    RUSSIAN("Русская"),
    FRENCH("Французская");

    private final String displayName;

    CuisineType(String displayName) {
        this.displayName = displayName;
    }

}