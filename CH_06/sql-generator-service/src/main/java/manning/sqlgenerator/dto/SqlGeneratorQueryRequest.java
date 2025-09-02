package manning.sqlgenerator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class SqlGeneratorQueryRequest {
    @NotBlank(message = "User prompt is required")
    @JsonProperty("user_prompt")
    private String userPrompt;

    @NotEmpty(message = "At least one table is required")
    @JsonProperty("tables")
    private List<Table> tables;

    // todo add last N queries field?

    // Default constructor
    public SqlGeneratorQueryRequest() {}

    public SqlGeneratorQueryRequest(String userPrompt, List<Table> tables) {
        this.userPrompt = userPrompt;
        this.tables = tables;
    }

    public String getUserPrompt() {
        return userPrompt;
    }

    public void setUserPrompt(String userPrompt) {
        this.userPrompt = userPrompt;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    @Override
    public String toString() {
        return "SqlGeneratorQueryRequest{" +
                "userPrompt='" + userPrompt + '\'' +
                ", tables=" + tables +
                '}';
    }
}
