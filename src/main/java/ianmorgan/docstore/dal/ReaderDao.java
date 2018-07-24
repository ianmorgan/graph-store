package ianmorgan.docstore.dal;

import java.util.Map;

/**
 * The basic reader behaviour,
 */
public interface ReaderDao {
    Map<String,Object> retrieve(String aggregateId);
}
