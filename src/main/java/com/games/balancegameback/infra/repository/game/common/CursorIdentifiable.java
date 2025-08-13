package com.games.balancegameback.infra.repository.game.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

public interface CursorIdentifiable {

    @JsonIgnore
    @Schema(hidden = true)
    Long getCursorValue();
}
