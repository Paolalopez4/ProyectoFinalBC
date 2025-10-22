package org.pasantia.ahorraya.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Locale;

/**
 * Enumeration of expense categories used throughout the application.
 *
 * <p>Provides a canonical set of categories and a case-insensitive factory
 * method to create an enum value from an incoming string. Common synonyms
 * (for example "GROCERY") are supported.</p>
 */
public enum ExpenseCategory {
    /** Food-related expenses (groceries, restaurants, etc.). */
    FOOD,

    /** Transportation-related expenses (taxi, transit, fuel, etc.). */
    TRANSPORTATION,

    /** Utility bills (electricity, water, gas, internet, etc.). */
    UTILITIES,

    /** Entertainment expenses (movies, events, streaming, etc.). */
    ENTERTAINMENT,

    /** Healthcare-related expenses (medical, pharmacy, insurance, etc.). */
    HEALTHCARE,

    /** Education-related expenses (tuition, courses, books, etc.). */
    EDUCATION,

    /** Personal care expenses (salon, toiletries, grooming, etc.). */
    PERSONAL_CARE,

    /** Miscellaneous or uncategorized expenses. */
    MISCELLANEOUS;

    /**
     * Creates an {@link ExpenseCategory} from a string key.
     *
     * <p>The lookup is case-insensitive and trims surrounding whitespace. Some
     * common synonyms are recognized (for example, {@code "GROCERY"} maps to {@link #FOOD}).</p>
     *
     * @param key the input string representing a category (must not be null)
     * @return the matching ExpenseCategory
     * @throws IllegalArgumentException if the key is null or does not match any known category
     */
    @JsonCreator
    public static ExpenseCategory fromString(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Category key must not be null");
        }
        switch (key.trim().toUpperCase(Locale.ROOT)) {
            case "FOOD":
            case "GROCERY":
                return FOOD;
            case "PERSONAL_CARE":
                return PERSONAL_CARE;
            case "ENTERTAINMENT":
                return ENTERTAINMENT;
            case "UTILITIES":
                return UTILITIES;
            case "TRANSPORTATION":
                return TRANSPORTATION;
            case "HEALTHCARE":
                return HEALTHCARE;
            case "MISCELLANEOUS":
                return MISCELLANEOUS;
            case "EDUCATION":
                return EDUCATION;
            default:
                throw new IllegalArgumentException("Unknown expense category: " + key);
        }
    }
}
