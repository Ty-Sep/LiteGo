package com.litego.tools;

public record SearchPlacesRequest(String address, String keywords, Integer radius) {
}