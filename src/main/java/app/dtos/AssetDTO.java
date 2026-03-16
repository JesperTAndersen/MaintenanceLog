package app.dtos;

import java.time.LocalDateTime;

public record AssetDTO
        (
                Integer id,
                String name,
                String description,
                boolean active,
                LocalDateTime lastLogDate
        )
{
}