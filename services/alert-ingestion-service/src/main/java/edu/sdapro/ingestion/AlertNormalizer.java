package edu.sdapro.ingestion;

import edu.sdapro.domain.CanonicalAlert;
import java.util.Map;

public interface AlertNormalizer {
  CanonicalAlert normalize(Map<String, Object> rawPayload);
}
