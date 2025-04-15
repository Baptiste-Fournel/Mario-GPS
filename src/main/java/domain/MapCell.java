package domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MapCell {
    private int x;
    private int y;
    private MapElementType type;
}
