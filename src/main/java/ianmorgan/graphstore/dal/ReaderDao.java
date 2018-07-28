package ianmorgan.graphstore.dal;

import java.util.Map;

/**
 * The basic reader behaviour. Any DAO must implement at least this.
 */
public interface ReaderDao {
    Map<String,Object> retrieve(String aggregateId);

    String aggregateKey();
}
