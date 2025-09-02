package manning.sqlgenerator.service;

import manning.sqlgenerator.dto.SqlGeneratorQueryRequest;
import manning.sqlgenerator.dto.SqlGeneratorQueryResponse;

public interface SqlGeneratorService {
    SqlGeneratorQueryResponse generateSql(SqlGeneratorQueryRequest request);
}
