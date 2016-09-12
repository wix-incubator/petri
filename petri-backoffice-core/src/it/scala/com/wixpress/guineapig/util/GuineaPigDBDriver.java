package com.wixpress.guineapig.util;

import java.util.Arrays;
import java.util.List;

public class GuineaPigDBDriver {

    private final List<GuineaPigTable> tables;

    private GuineaPigDBDriver(List<GuineaPigTable> tables) {
        this.tables = tables;
    }

    public static GuineaPigDBDriver dbDriver(GuineaPigTable... tables) {
        return new GuineaPigDBDriver(Arrays.asList(tables));
    }

    public void reloadSchema() {
        dropTables();
        createTables();
    }

    public void dropTables() {
        tables.forEach(GuineaPigTable::dropTable);

    }

    public void createTables() {
        tables.forEach(GuineaPigTable::createTable);
    }
}
