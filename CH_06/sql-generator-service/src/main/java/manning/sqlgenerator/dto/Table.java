package manning.sqlgenerator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class Table {
    @NotBlank(message = "Table name is required")
    @JsonProperty("tableName")
    private String tableName;

    @JsonProperty("createTableSql")
    private String createTableSql;

    public Table() {}

    public Table(String tableName) {
        this.tableName = tableName;
    }

    public Table(String tableName, String createTableSql) {
        this.tableName = tableName;
        this.createTableSql = createTableSql;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getCreateTableSql() {
        return createTableSql;
    }

    public void setCreateTableSql(String createTableSql) {
        this.createTableSql = createTableSql;
    }

    @Override
    public String toString() {
        return "Table{" +
                "tableName='" + tableName + '\'' +
                ", createTableSql='" + createTableSql + '\'' +
                '}';
    }
}
