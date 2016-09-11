package com.wixpress.guineapig.entities.ui;

import com.wixpress.petri.experiments.domain.TestGroup;

public class UiTestGroup {
    private String value;
    private int chunk;
    private int id;

    public UiTestGroup() {
    }


    public UiTestGroup(int id, String value, int chunk) {

        this.value = value;
        this.chunk = chunk;
        this.id = id;
    }


    public String getValue() {
        return value;
    }

    public int getChunk() {
        return chunk;
    }

    public int getId() {
        return id;
    }

    public TestGroup toTestGroup() {

        return new TestGroup(id, chunk, value);
    }

    public static UiTestGroup fromTestGroup(TestGroup tg) {
        return new UiTestGroup(tg.getId(), tg.getValue(), tg.getChunk());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UiTestGroup that = (UiTestGroup) o;

        if (chunk != that.chunk) return false;
        if (id != that.id) return false;
        if (!value.equals(that.value)) return false;

        return true;
    }

    @Override
    public String toString() {
        return "UiTestGroup{" +
                "value='" + value + '\'' +
                ", chunk=" + chunk +
                ", id=" + id +
                '}';
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + chunk;
        result = 31 * result + id;
        return result;
    }


}
