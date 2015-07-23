package com.wixpress.petri.experiments.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestGroup {
    int id;
    private int chunk;
    private String value;

    public TestGroup(int id, int chunk, String value) {
        this.id = id;
        this.chunk = chunk;
        this.value = value;
    }

    public TestGroup() {
        ;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setChunk(int chunk) {
        this.chunk = chunk;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public int getChunk() {
        return chunk;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestGroup)) return false;

        TestGroup testGroup = (TestGroup) o;

        if (chunk != testGroup.chunk) return false;
        if (id != testGroup.id) return false;
        if (value != null ? !value.equals(testGroup.value) : testGroup.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + chunk;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TestGroup{" +
                "id=" + id +
                ", chunk=" + chunk +
                ", value='" + value + '\'' +
                '}';
    }
}
