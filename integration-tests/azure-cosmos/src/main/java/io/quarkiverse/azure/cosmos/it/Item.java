package io.quarkiverse.azure.cosmos.it;

public class Item {
    public static final String PARTITION_KEY = "/id";

    private String id;
    private String name;

    public Item() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
