package net.runelite.client.plugins.iutils.scene;

public enum ObjectType {
    WALL(ObjectCategory.WALL),
    WALL_CONNECTOR(ObjectCategory.WALL),
    WALL_CORNER(ObjectCategory.WALL),
    WALL_PILLAR(ObjectCategory.WALL),

    WALL_DECORATION(ObjectCategory.WALL_DECORATION),
    WALL_DECORATION_OPPOSITE(ObjectCategory.WALL_DECORATION),
    WALL_DECORATION_DIAGONAL(ObjectCategory.WALL_DECORATION),
    WALL_DECORATION_OPPOSITE_DIAGONAL(ObjectCategory.WALL_DECORATION),
    WALL_DECORATION_DOUBLE(ObjectCategory.WALL_DECORATION),

    DIAGONAL_WALL(ObjectCategory.REGULAR),
    OBJECT(ObjectCategory.REGULAR),
    OBJECT_DIAGONAL(ObjectCategory.REGULAR),
    ROOF_SLOPE(ObjectCategory.REGULAR),
    ROOF_SLOPE_DIAGONAL(ObjectCategory.REGULAR),
    ROOF_HALF_SLOPE_DIAGONAL(ObjectCategory.REGULAR),
    ROOF_SLOPE_OUTER_CONNECTOR(ObjectCategory.REGULAR),
    ROOF_SLOPE_INNER_CONNECTOR(ObjectCategory.REGULAR),
    ROOF_FLAT(ObjectCategory.REGULAR),
    ROOF_EDGE_SLOPE(ObjectCategory.REGULAR),
    ROOF_EDGE_INNER_CONNECTOR(ObjectCategory.REGULAR),
    ROOF_EDGE_OUTER_CONNECTOR_TRIANGLE(ObjectCategory.REGULAR),
    ROOF_EDGE_OUTER_CONNECTOR_SQUARE(ObjectCategory.REGULAR),

    FLOOR_DECORATION(ObjectCategory.FLOOR_DECORATION);

    public final ObjectCategory category;

    ObjectType(ObjectCategory category) {
        this.category = category;
    }
}
