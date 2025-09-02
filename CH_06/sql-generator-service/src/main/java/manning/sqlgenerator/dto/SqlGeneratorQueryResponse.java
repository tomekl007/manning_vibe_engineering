package manning.sqlgenerator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SqlGeneratorQueryResponse {
    @JsonProperty("sql")
    private String sql;

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("message")
    private String message;

    public SqlGeneratorQueryResponse() {}

    public SqlGeneratorQueryResponse(String sql) {
        this.sql = sql;
        this.success = true;
        this.message = "SQL generated successfully";
    }

    public SqlGeneratorQueryResponse(String sql, boolean success, String message) {
        this.sql = sql;
        this.success = success;
        this.message = message;
    }

    public static SqlGeneratorQueryResponse success(String sql) {
        return new SqlGeneratorQueryResponse(sql, true, "SQL generated successfully");
    }

    public static SqlGeneratorQueryResponse error(String message) {
        return new SqlGeneratorQueryResponse(null, false, message);
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "SqlGeneratorQueryResponse{" +
                "sql='" + sql + '\'' +
                ", success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}
