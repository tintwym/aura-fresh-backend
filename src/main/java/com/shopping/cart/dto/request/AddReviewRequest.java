package com.shopping.cart.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AddReviewRequest {
    @NotBlank
    private String comment;

    @Min(1)
    @Max(5)
    private int rating;

    @NotNull
    private UUID productId;

    @NotNull
    private UUID orderItemId;
}
